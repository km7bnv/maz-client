using System.IO;
using System.Net.Http;
using System.Text.Json;

namespace MazLauncher;

public sealed class ManagedResourcePackService
{
    private static readonly HttpClient Http = new();
    private static readonly string DataRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher");

    private static readonly (string Slug, string Marker)[] Packs =
    {
        ("lower-fire", ".maz-low-fire-pack"),
        ("low-shield-pvp", ".maz-low-shield-pack"),
        ("small-low-totem", ".maz-small-totem-pack")
    };

    static ManagedResourcePackService()
    {
        Http.Timeout = TimeSpan.FromSeconds(8);
        Http.DefaultRequestHeaders.UserAgent.ParseAdd($"MazLauncher/{CloudUpdateService.CurrentLauncherVersion}");
    }

    public async Task EnsureForMazClientAsync(string mazClientVersion, Action<string>? log = null)
    {
        if (string.IsNullOrWhiteSpace(mazClientVersion)) return;
        var gameDir = Path.Combine(DataRoot, "installations", "mazclient", SafeName(mazClientVersion));
        var resourcePacksDir = Path.Combine(gameDir, "resourcepacks");
        Directory.CreateDirectory(resourcePacksDir);

        var enabledFiles = new List<string>();
        foreach (var (slug, markerName) in Packs)
        {
            try
            {
                var file = await EnsurePackAsync(resourcePacksDir, slug, markerName, log);
                if (!string.IsNullOrWhiteSpace(file)) enabledFiles.Add(file);
            }
            catch (Exception ex)
            {
                log?.Invoke($"Managed resource pack {slug} unavailable: {ex.Message}");
                var cached = ReadMarker(resourcePacksDir, markerName);
                if (!string.IsNullOrWhiteSpace(cached) && File.Exists(Path.Combine(resourcePacksDir, cached))) enabledFiles.Add(cached);
            }
        }

        if (enabledFiles.Count > 0)
        {
            EnablePacks(Path.Combine(gameDir, "options.txt"), enabledFiles);
            log?.Invoke("Low Fire + Low Shield + Smaller Totem resource packs ready and enabled");
        }
    }

    private static async Task<string> EnsurePackAsync(string resourcePacksDir, string slug, string markerName, Action<string>? log)
    {
        var query = $"https://api.modrinth.com/v2/project/{slug}/version?game_versions=%5B%22{LauncherService.MinecraftVersion}%22%5D";
        using var response = await Http.GetAsync(query);
        response.EnsureSuccessStatusCode();
        await using var stream = await response.Content.ReadAsStreamAsync();
        using var doc = await JsonDocument.ParseAsync(stream);

        JsonElement? selected = null;
        foreach (var version in doc.RootElement.EnumerateArray())
        {
            if (version.TryGetProperty("version_type", out var type) && string.Equals(type.GetString(), "release", StringComparison.OrdinalIgnoreCase))
            {
                selected = version;
                break;
            }
        }
        if (selected == null) throw new InvalidOperationException($"No Minecraft {LauncherService.MinecraftVersion} release found on Modrinth.");

        var files = selected.Value.GetProperty("files");
        JsonElement selectedFile = files[0];
        foreach (var file in files.EnumerateArray())
            if (file.TryGetProperty("primary", out var primary) && primary.GetBoolean()) { selectedFile = file; break; }

        var fileName = selectedFile.GetProperty("filename").GetString() ?? throw new InvalidDataException("Pack filename missing.");
        var url = selectedFile.GetProperty("url").GetString() ?? throw new InvalidDataException("Pack URL missing.");
        var target = Path.Combine(resourcePacksDir, fileName);
        var oldFile = ReadMarker(resourcePacksDir, markerName);

        if (!File.Exists(target))
        {
            log?.Invoke($"Downloading managed resource pack: {slug}");
            var temp = target + "." + Guid.NewGuid().ToString("N") + ".download";
            try
            {
                await using var source = await Http.GetStreamAsync(url);
                await using var destination = new FileStream(temp, FileMode.CreateNew, FileAccess.Write, FileShare.None);
                await source.CopyToAsync(destination);
                await destination.FlushAsync();
                File.Move(temp, target, true);
            }
            finally
            {
                if (File.Exists(temp)) File.Delete(temp);
            }
        }

        if (!string.IsNullOrWhiteSpace(oldFile) && !string.Equals(oldFile, fileName, StringComparison.OrdinalIgnoreCase))
        {
            var oldPath = Path.Combine(resourcePacksDir, oldFile);
            if (File.Exists(oldPath)) File.Delete(oldPath);
        }

        File.WriteAllText(Path.Combine(resourcePacksDir, markerName), fileName);
        return fileName;
    }

    private static void EnablePacks(string optionsPath, IReadOnlyList<string> fileNames)
    {
        var lines = File.Exists(optionsPath) ? File.ReadAllLines(optionsPath).ToList() : new List<string>();
        var index = lines.FindIndex(line => line.StartsWith("resourcePacks:", StringComparison.Ordinal));
        List<string> packs;
        if (index >= 0)
        {
            var json = lines[index]["resourcePacks:".Length..];
            try { packs = JsonSerializer.Deserialize<List<string>>(json) ?? new List<string>(); }
            catch { packs = new List<string>(); }
        }
        else
        {
            packs = new List<string>();
        }

        foreach (var fileName in fileNames.Reverse())
        {
            var entry = "file/" + fileName;
            packs.RemoveAll(existing => string.Equals(existing, entry, StringComparison.OrdinalIgnoreCase));
            packs.Insert(0, entry);
        }

        var resourceLine = "resourcePacks:" + JsonSerializer.Serialize(packs);
        if (index >= 0) lines[index] = resourceLine;
        else lines.Add(resourceLine);
        if (!lines.Any(line => line.StartsWith("incompatibleResourcePacks:", StringComparison.Ordinal)))
            lines.Add("incompatibleResourcePacks:[]");
        Directory.CreateDirectory(Path.GetDirectoryName(optionsPath)!);
        File.WriteAllLines(optionsPath, lines);
    }

    private static string ReadMarker(string resourcePacksDir, string markerName)
    {
        var path = Path.Combine(resourcePacksDir, markerName);
        try { return File.Exists(path) ? File.ReadAllText(path).Trim() : string.Empty; }
        catch { return string.Empty; }
    }

    private static string SafeName(string value)
    {
        var invalid = Path.GetInvalidFileNameChars();
        return new string(value.Select(ch => invalid.Contains(ch) ? '_' : ch).ToArray());
    }
}
