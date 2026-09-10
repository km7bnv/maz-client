using System.IO;
using System.Net.Http;
using System.Text.Json;

namespace MazLauncher;

public sealed class BetterBlockEntitiesService
{
    private static readonly HttpClient Http = new();
    private static readonly string DataRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher");
    private const string MarkerName = ".maz-better-block-entities";

    static BetterBlockEntitiesService()
    {
        Http.Timeout = TimeSpan.FromSeconds(15);
        Http.DefaultRequestHeaders.UserAgent.ParseAdd($"MazLauncher/{CloudUpdateService.CurrentLauncherVersion}");
    }

    public async Task EnsureForMazClientAsync(string mazClientVersion, Action<string>? log = null)
    {
        if (string.IsNullOrWhiteSpace(mazClientVersion)) return;
        var modsDir = Path.Combine(DataRoot, "installations", "mazclient", SafeName(mazClientVersion), "mods");
        Directory.CreateDirectory(modsDir);
        var markerPath = Path.Combine(modsDir, MarkerName);
        var cachedName = ReadMarker(markerPath);
        var cachedPath = string.IsNullOrWhiteSpace(cachedName) ? string.Empty : Path.Combine(modsDir, cachedName);

        try
        {
            var query = $"https://api.modrinth.com/v2/project/better-block-entities/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%22{LauncherService.MinecraftVersion}%22%5D";
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
            if (selected == null) throw new InvalidOperationException("No stable Better Block Entities Fabric release is available for Minecraft 26.2.");

            var files = selected.Value.GetProperty("files");
            if (files.GetArrayLength() == 0) throw new InvalidDataException("Better Block Entities release contains no files.");
            var selectedFile = files[0];
            foreach (var file in files.EnumerateArray())
                if (file.TryGetProperty("primary", out var primary) && primary.GetBoolean()) { selectedFile = file; break; }

            var fileName = selectedFile.GetProperty("filename").GetString() ?? throw new InvalidDataException("Better Block Entities filename missing.");
            var url = selectedFile.GetProperty("url").GetString() ?? throw new InvalidDataException("Better Block Entities URL missing.");
            var target = Path.Combine(modsDir, fileName);

            if (!File.Exists(target))
            {
                log?.Invoke("Downloading managed optimization: Better Block Entities");
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
                    if (new FileInfo(temp).Length < 10_000) throw new InvalidDataException("Downloaded Better Block Entities JAR is unexpectedly small.");
                    File.Move(temp, target, true);
                }
                finally { if (File.Exists(temp)) File.Delete(temp); }
            }

            if (!string.IsNullOrWhiteSpace(cachedName) && !string.Equals(cachedName, fileName, StringComparison.OrdinalIgnoreCase) && File.Exists(cachedPath))
                File.Delete(cachedPath);
            File.WriteAllText(markerPath, fileName);
            log?.Invoke("Better Block Entities verified and cached");
        }
        catch when (!string.IsNullOrWhiteSpace(cachedName) && File.Exists(cachedPath) && new FileInfo(cachedPath).Length >= 10_000)
        {
            log?.Invoke("Using cached Better Block Entities while offline");
        }
    }

    private static string ReadMarker(string path)
    {
        try { return File.Exists(path) ? File.ReadAllText(path).Trim() : string.Empty; }
        catch { return string.Empty; }
    }

    private static string SafeName(string value)
    {
        var invalid = Path.GetInvalidFileNameChars();
        return new string(value.Select(ch => invalid.Contains(ch) ? '_' : ch).ToArray());
    }
}
