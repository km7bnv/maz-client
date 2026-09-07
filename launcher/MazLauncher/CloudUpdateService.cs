using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Reflection;
using System.Security.Cryptography;
using System.Text.Json;

namespace MazLauncher;

public sealed class CloudUpdateService
{
    private const string ManifestUrl = "https://github.com/km7bnv/maz-client/releases/download/cloud/latest.json";
    private const string MazClientJarUrl = "https://github.com/km7bnv/maz-client/releases/download/cloud/maz-client.jar";
    private const string ReleasesBaseUrl = "https://github.com/km7bnv/maz-client/releases/download";

    private static readonly string CacheDir = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "MazLauncher",
        "cache"
    );
    private static readonly string ManifestCachePath = Path.Combine(CacheDir, "latest-cloud-manifest.json");

    private readonly HttpClient http = new();

    public CloudUpdateService()
    {
        Directory.CreateDirectory(CacheDir);
        http.DefaultRequestHeaders.UserAgent.ParseAdd($"MazLauncher/{CurrentLauncherVersion}");
    }

    public async Task<CloudManifest?> GetManifestAsync()
    {
        try
        {
            var json = await http.GetStringAsync(ManifestUrl);
            var manifest = JsonSerializer.Deserialize<CloudManifest>(json, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });
            if (manifest != null)
                await File.WriteAllTextAsync(ManifestCachePath, json);
            return manifest;
        }
        catch
        {
            try
            {
                if (!File.Exists(ManifestCachePath)) return null;
                var cachedJson = await File.ReadAllTextAsync(ManifestCachePath);
                return JsonSerializer.Deserialize<CloudManifest>(cachedJson, new JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true
                });
            }
            catch
            {
                return null;
            }
        }
    }

    public static string CurrentLauncherVersion =>
        Assembly.GetExecutingAssembly().GetName().Version?.ToString(3) ?? "0.0.0";

    public async Task<bool> HasLauncherUpdateAsync()
    {
        var manifest = await GetManifestAsync();
        if (manifest == null || !Version.TryParse(manifest.LauncherVersion, out var remote))
            return false;

        return remote > Version.Parse(CurrentLauncherVersion);
    }

    public async Task<string?> EnsureMazClientCurrentAsync(string gameDir, Action<string, int>? progress = null)
    {
        var manifest = await GetManifestAsync();
        if (manifest == null || string.IsNullOrWhiteSpace(manifest.MazClientVersion))
            return null;

        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);
        Directory.CreateDirectory(CacheDir);

        var targetJar = Path.Combine(modsDir, "maz-client.jar");
        var versionMarker = Path.Combine(CacheDir, "maz-client-version.txt");
        var installedVersion = File.Exists(versionMarker) ? (await File.ReadAllTextAsync(versionMarker)).Trim() : string.Empty;

        var expectedHash = manifest.MazClientSha256?.Trim().ToLowerInvariant() ?? string.Empty;
        var hashMatches = File.Exists(targetJar) && !string.IsNullOrWhiteSpace(expectedHash) &&
                          string.Equals(await ComputeSha256Async(targetJar), expectedHash, StringComparison.OrdinalIgnoreCase);

        if (File.Exists(targetJar) && installedVersion == manifest.MazClientVersion && hashMatches)
            return manifest.MazClientVersion;

        progress?.Invoke($"Updating MazClient to {manifest.MazClientVersion}...", 30);
        var tempJar = targetJar + ".download";
        try
        {
            await using (var source = await http.GetStreamAsync(MazClientJarUrl))
            await using (var destination = File.Create(tempJar))
                await source.CopyToAsync(destination);

            if (!string.IsNullOrWhiteSpace(expectedHash))
            {
                var downloadedHash = await ComputeSha256Async(tempJar);
                if (!string.Equals(downloadedHash, expectedHash, StringComparison.OrdinalIgnoreCase))
                    throw new InvalidDataException("MazClient cloud download failed SHA256 verification.");
            }

            File.Move(tempJar, targetJar, true);
            await File.WriteAllTextAsync(versionMarker, manifest.MazClientVersion);
            return manifest.MazClientVersion;
        }
        finally
        {
            if (File.Exists(tempJar)) File.Delete(tempJar);
        }
    }

    private static async Task<string> ComputeSha256Async(string path)
    {
        await using var stream = File.OpenRead(path);
        var hash = await SHA256.HashDataAsync(stream);
        return Convert.ToHexString(hash).ToLowerInvariant();
    }

    public async Task DownloadAndApplyLauncherUpdateAsync()
    {
        var manifest = await GetManifestAsync();
        if (manifest == null)
            throw new InvalidOperationException("Could not reach the MazLauncher update server.");

        if (!Version.TryParse(manifest.LauncherVersion, out var remoteVersion))
            throw new InvalidDataException("The cloud manifest contains an invalid MazLauncher version.");

        if (remoteVersion <= Version.Parse(CurrentLauncherVersion))
            return;

        if (string.IsNullOrWhiteSpace(manifest.MazClientVersion))
            throw new InvalidDataException("The cloud manifest does not contain a MazClient version.");

        var installerName = $"MazClient-{manifest.MazClientVersion}-MazLauncher-{manifest.LauncherVersion}-Setup.exe";
        var installerUrl = $"{ReleasesBaseUrl}/mazlauncher-v{manifest.LauncherVersion}/{installerName}";
        var tempRoot = Path.Combine(Path.GetTempPath(), "MazLauncherInstaller-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(tempRoot);
        var installerPath = Path.Combine(tempRoot, installerName);

        await using (var source = await http.GetStreamAsync(installerUrl))
        await using (var destination = new FileStream(installerPath, FileMode.CreateNew, FileAccess.Write, FileShare.None))
        {
            await source.CopyToAsync(destination);
            await destination.FlushAsync();
        }

        if (!File.Exists(installerPath) || new FileInfo(installerPath).Length < 1024 * 1024)
            throw new InvalidDataException("Downloaded MazLauncher installer is missing or unexpectedly small.");

        Process.Start(new ProcessStartInfo
        {
            FileName = installerPath,
            Arguments = "/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /CLOSEAPPLICATIONS",
            UseShellExecute = true,
            WorkingDirectory = tempRoot
        });

        Environment.Exit(0);
    }
}

public sealed class CloudManifest
{
    public string MazClientVersion { get; set; } = "unknown";
    public string MazClientSha256 { get; set; } = string.Empty;
    public string LauncherVersion { get; set; } = "0.0.0";
}
