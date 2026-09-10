using System.IO;
using System.IO.Compression;
using System.Net.Http;
using System.Text.Json;

namespace MazLauncher;

public sealed class GnetumService
{
    private static readonly HttpClient Http = new();
    private static readonly string DataRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher");
    private const string MarkerName = ".maz-gnetum";
    private const string ExpectedModId = "gnetum";

    static GnetumService()
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

        if (!string.IsNullOrWhiteSpace(cachedPath) && File.Exists(cachedPath) && !IsValidGnetumJar(cachedPath))
        {
            log?.Invoke("Discarding invalid cached Gnetum JAR");
            TryDelete(cachedPath);
            TryDelete(markerPath);
            cachedName = string.Empty;
            cachedPath = string.Empty;
        }

        try
        {
            var query = $"https://api.modrinth.com/v2/project/gnetum/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%22{LauncherService.MinecraftVersion}%22%5D";
            using var response = await Http.GetAsync(query);
            response.EnsureSuccessStatusCode();
            await using var stream = await response.Content.ReadAsStreamAsync();
            using var doc = await JsonDocument.ParseAsync(stream);

            JsonElement? stable = null;
            JsonElement? beta = null;
            foreach (var version in doc.RootElement.EnumerateArray())
            {
                if (!version.TryGetProperty("version_type", out var type)) continue;
                var versionType = type.GetString();
                if (stable == null && string.Equals(versionType, "release", StringComparison.OrdinalIgnoreCase))
                {
                    stable = version;
                    break;
                }
                if (beta == null && string.Equals(versionType, "beta", StringComparison.OrdinalIgnoreCase)) beta = version;
            }

            var selected = stable ?? beta;
            if (selected == null)
                throw new InvalidOperationException($"No compatible Gnetum Fabric release or beta is available for Minecraft {LauncherService.MinecraftVersion}.");

            var files = selected.Value.GetProperty("files");
            if (files.GetArrayLength() == 0) throw new InvalidDataException("Gnetum release contains no files.");

            var selectedFile = files[0];
            foreach (var file in files.EnumerateArray())
            {
                if (file.TryGetProperty("primary", out var primary) && primary.GetBoolean()) { selectedFile = file; break; }
            }

            var fileName = selectedFile.GetProperty("filename").GetString() ?? throw new InvalidDataException("Gnetum filename missing.");
            var url = selectedFile.GetProperty("url").GetString() ?? throw new InvalidDataException("Gnetum URL missing.");
            var target = Path.Combine(modsDir, fileName);

            if (File.Exists(target) && !IsValidGnetumJar(target)) TryDelete(target);
            if (!File.Exists(target))
            {
                log?.Invoke("Downloading managed optimization: Gnetum");
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
                    if (!IsValidGnetumJar(temp)) throw new InvalidDataException("Downloaded Gnetum file is not the expected Fabric mod JAR.");
                    File.Move(temp, target, true);
                }
                finally { TryDelete(temp); }
            }

            if (!IsValidGnetumJar(target)) throw new InvalidDataException("Gnetum cache validation failed.");
            if (!string.IsNullOrWhiteSpace(cachedName) && !string.Equals(cachedName, fileName, StringComparison.OrdinalIgnoreCase) && File.Exists(cachedPath)) TryDelete(cachedPath);

            File.WriteAllText(markerPath, fileName);
            log?.Invoke("Gnetum Fabric JAR verified and cached");
        }
        catch when (!string.IsNullOrWhiteSpace(cachedName) && File.Exists(cachedPath) && IsValidGnetumJar(cachedPath))
        {
            log?.Invoke("Using validated cached Gnetum while offline");
        }
    }

    private static bool IsValidGnetumJar(string path)
    {
        if (string.IsNullOrWhiteSpace(path) || !File.Exists(path)) return false;
        try
        {
            using var archive = ZipFile.OpenRead(path);
            var metadata = archive.Entries.FirstOrDefault(entry => string.Equals(entry.FullName.Replace('\\', '/'), "fabric.mod.json", StringComparison.OrdinalIgnoreCase));
            if (metadata == null || metadata.Length == 0) return false;
            using var metadataStream = metadata.Open();
            using var doc = JsonDocument.Parse(metadataStream);
            return doc.RootElement.ValueKind == JsonValueKind.Object
                && doc.RootElement.TryGetProperty("id", out var id)
                && id.ValueKind == JsonValueKind.String
                && string.Equals(id.GetString(), ExpectedModId, StringComparison.OrdinalIgnoreCase);
        }
        catch { return false; }
    }

    private static string ReadMarker(string path) { try { return File.Exists(path) ? File.ReadAllText(path).Trim() : string.Empty; } catch { return string.Empty; } }
    private static void TryDelete(string path) { try { if (!string.IsNullOrWhiteSpace(path) && File.Exists(path)) File.Delete(path); } catch { } }
    private static string SafeName(string value) { var invalid = Path.GetInvalidFileNameChars(); return new string(value.Select(ch => invalid.Contains(ch) ? '_' : ch).ToArray()); }
}
