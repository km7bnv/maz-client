using System.IO;
using System.IO.Compression;
using System.Net.Http;
using System.Text.Json;

namespace MazLauncher;

public sealed class ManagedResourcePackService
{
    private static readonly HttpClient Http = new();
    private static readonly string DataRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher");
    private const int MaxAttempts = 3;

    private static readonly (string Slug, string Marker, string DisplayName)[] Packs =
    {
        ("lower-fire", ".maz-low-fire-pack", "Low Fire"),
        ("low-shield-pvp", ".maz-low-shield-pack", "Low Shield PvP"),
        ("small-low-totem", ".maz-small-totem-pack", "Smaller Totem")
    };

    static ManagedResourcePackService()
    {
        Http.Timeout = TimeSpan.FromSeconds(15);
        Http.DefaultRequestHeaders.UserAgent.ParseAdd($"MazLauncher/{CloudUpdateService.CurrentLauncherVersion}");
    }

    public async Task EnsureForMazClientAsync(string mazClientVersion, Action<string>? log = null)
    {
        if (string.IsNullOrWhiteSpace(mazClientVersion))
            throw new ArgumentException("MazClient version is required before preparing managed resource packs.", nameof(mazClientVersion));

        var gameDir = Path.Combine(DataRoot, "installations", "mazclient", SafeName(mazClientVersion));
        var resourcePacksDir = Path.Combine(gameDir, "resourcepacks");
        Directory.CreateDirectory(resourcePacksDir);

        var enabledFiles = new List<string>();
        var missingPacks = new List<string>();

        foreach (var (slug, markerName, displayName) in Packs)
        {
            try
            {
                var file = await EnsurePackWithRetryAsync(resourcePacksDir, slug, markerName, displayName, log);
                enabledFiles.Add(file);
            }
            catch (Exception ex)
            {
                log?.Invoke($"Managed resource pack {displayName} online refresh failed: {ex.Message}");

                var cached = ReadMarker(resourcePacksDir, markerName);
                var cachedPath = string.IsNullOrWhiteSpace(cached) ? string.Empty : Path.Combine(resourcePacksDir, cached);
                if (!string.IsNullOrWhiteSpace(cached) && IsValidZip(cachedPath))
                {
                    enabledFiles.Add(cached);
                    log?.Invoke($"Using cached managed resource pack: {displayName}");
                }
                else
                {
                    missingPacks.Add(displayName);
                }
            }
        }

        if (missingPacks.Count > 0)
        {
            throw new InvalidOperationException(
                "MazLauncher could not prepare required resource pack(s): " + string.Join(", ", missingPacks) +
                ". Connect to the internet and retry so MazLauncher can repair the managed resource-pack cache.");
        }

        EnablePacks(Path.Combine(gameDir, "options.txt"), enabledFiles);
        log?.Invoke("Low Fire + Low Shield PvP + Smaller Totem resource packs verified, cached, and enabled");
    }

    private static async Task<string> EnsurePackWithRetryAsync(
        string resourcePacksDir,
        string slug,
        string markerName,
        string displayName,
        Action<string>? log)
    {
        Exception? lastError = null;
        for (var attempt = 1; attempt <= MaxAttempts; attempt++)
        {
            try
            {
                return await EnsurePackOnlineAsync(resourcePacksDir, slug, markerName, displayName, log);
            }
            catch (Exception ex) when (attempt < MaxAttempts)
            {
                lastError = ex;
                log?.Invoke($"{displayName} attempt {attempt}/{MaxAttempts} failed; retrying...");
                await Task.Delay(TimeSpan.FromMilliseconds(350 * attempt));
            }
            catch (Exception ex)
            {
                lastError = ex;
            }
        }

        throw new InvalidOperationException($"{displayName} could not be refreshed after {MaxAttempts} attempts.", lastError);
    }

    private static async Task<string> EnsurePackOnlineAsync(
        string resourcePacksDir,
        string slug,
        string markerName,
        string displayName,
        Action<string>? log)
    {
        var query = $"https://api.modrinth.com/v2/project/{slug}/version?game_versions=%5B%22{LauncherService.MinecraftVersion}%22%5D";
        using var response = await Http.GetAsync(query);
        response.EnsureSuccessStatusCode();
        await using var stream = await response.Content.ReadAsStreamAsync();
        using var doc = await JsonDocument.ParseAsync(stream);

        JsonElement? selected = null;
        foreach (var version in doc.RootElement.EnumerateArray())
        {
            if (version.TryGetProperty("version_type", out var type)
                && string.Equals(type.GetString(), "release", StringComparison.OrdinalIgnoreCase))
            {
                selected = version;
                break;
            }
        }

        if (selected == null)
            throw new InvalidOperationException($"No Minecraft {LauncherService.MinecraftVersion} release found on Modrinth.");

        var files = selected.Value.GetProperty("files");
        if (files.GetArrayLength() == 0)
            throw new InvalidDataException($"{displayName} release contains no downloadable files.");

        JsonElement selectedFile = files[0];
        foreach (var file in files.EnumerateArray())
        {
            if (file.TryGetProperty("primary", out var primary) && primary.GetBoolean())
            {
                selectedFile = file;
                break;
            }
        }

        var fileName = selectedFile.GetProperty("filename").GetString() ?? throw new InvalidDataException("Pack filename missing.");
        var url = selectedFile.GetProperty("url").GetString() ?? throw new InvalidDataException("Pack URL missing.");
        var target = Path.Combine(resourcePacksDir, fileName);
        var oldFile = ReadMarker(resourcePacksDir, markerName);

        if (File.Exists(target) && !IsValidZip(target))
        {
            log?.Invoke($"Repairing invalid cached managed resource pack: {displayName}");
            File.Delete(target);
        }

        if (!File.Exists(target))
        {
            log?.Invoke($"Downloading managed resource pack: {displayName}");
            var temp = target + "." + Guid.NewGuid().ToString("N") + ".download";
            try
            {
                using var download = await Http.GetAsync(url, HttpCompletionOption.ResponseHeadersRead);
                download.EnsureSuccessStatusCode();
                await using var source = await download.Content.ReadAsStreamAsync();
                await using (var destination = new FileStream(temp, FileMode.CreateNew, FileAccess.Write, FileShare.None))
                {
                    await source.CopyToAsync(destination);
                    await destination.FlushAsync();
                }

                if (!IsValidZip(temp))
                    throw new InvalidDataException($"Downloaded {displayName} file is not a valid resource-pack ZIP.");

                File.Move(temp, target, true);
            }
            finally
            {
                if (File.Exists(temp)) File.Delete(temp);
            }
        }

        if (!IsValidZip(target))
            throw new InvalidDataException($"{displayName} cache validation failed.");

        if (!string.IsNullOrWhiteSpace(oldFile) && !string.Equals(oldFile, fileName, StringComparison.OrdinalIgnoreCase))
        {
            var oldPath = Path.Combine(resourcePacksDir, oldFile);
            if (File.Exists(oldPath)) File.Delete(oldPath);
        }

        File.WriteAllText(Path.Combine(resourcePacksDir, markerName), fileName);
        return fileName;
    }

    private static bool IsValidZip(string path)
    {
        if (string.IsNullOrWhiteSpace(path) || !File.Exists(path)) return false;
        try
        {
            var info = new FileInfo(path);
            if (info.Length < 4) return false;
            using var archive = ZipFile.OpenRead(path);
            return archive.Entries.Count > 0;
        }
        catch
        {
            return false;
        }
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
