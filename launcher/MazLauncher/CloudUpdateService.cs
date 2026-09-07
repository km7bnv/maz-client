using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Net.Http;
using System.Reflection;
using System.Security.Cryptography;
using System.Text.Json;

namespace MazLauncher;

public sealed class CloudUpdateService
{
    private const string ManifestUrl = "https://github.com/km7bnv/maz-client/releases/download/cloud/latest.json";
    private const string LauncherZipUrl = "https://github.com/km7bnv/maz-client/releases/download/cloud/MazLauncher-win-x64.zip";
    private const string MazClientJarUrl = "https://github.com/km7bnv/maz-client/releases/download/cloud/maz-client.jar";

    private readonly HttpClient http = new();

    public CloudUpdateService()
    {
        http.DefaultRequestHeaders.UserAgent.ParseAdd($"MazLauncher/{CurrentLauncherVersion}");
    }

    public async Task<CloudManifest?> GetManifestAsync()
    {
        try
        {
            var json = await http.GetStringAsync(ManifestUrl);
            return JsonSerializer.Deserialize<CloudManifest>(json, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });
        }
        catch
        {
            return null;
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
        var cacheDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher", "cache");
        Directory.CreateDirectory(modsDir);
        Directory.CreateDirectory(cacheDir);

        var targetJar = Path.Combine(modsDir, "maz-client.jar");
        var versionMarker = Path.Combine(cacheDir, "maz-client-version.txt");
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

    private static void EnsureAppDirectoryWritable(string appDir)
    {
        var probe = Path.Combine(appDir, $".mazlauncher-write-test-{Guid.NewGuid():N}.tmp");
        try
        {
            File.WriteAllText(probe, "ok");
            File.Delete(probe);
        }
        catch (Exception ex)
        {
            throw new UnauthorizedAccessException(
                "MazLauncher cannot self-update from this install location. Install the latest per-user build to enable automatic launcher updates.", ex);
        }
    }

    public async Task DownloadAndApplyLauncherUpdateAsync()
    {
        var appDir = AppContext.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
        EnsureAppDirectoryWritable(appDir);

        var tempRoot = Path.Combine(Path.GetTempPath(), "MazLauncherUpdate-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(tempRoot);
        var zipPath = Path.Combine(tempRoot, "launcher.zip");
        var extractDir = Path.Combine(tempRoot, "new");
        var backupDir = Path.Combine(tempRoot, "backup");

        await using (var source = await http.GetStreamAsync(LauncherZipUrl))
        await using (var target = File.Create(zipPath))
            await source.CopyToAsync(target);

        ZipFile.ExtractToDirectory(zipPath, extractDir);
        if (!File.Exists(Path.Combine(extractDir, "MazLauncher.exe")))
            throw new InvalidDataException("Downloaded MazLauncher update does not contain MazLauncher.exe.");

        var exePath = Path.Combine(appDir, "MazLauncher.exe");
        var scriptPath = Path.Combine(tempRoot, "update.ps1");
        var escapedApp = appDir.Replace("'", "''");
        var escapedNew = extractDir.Replace("'", "''");
        var escapedBackup = backupDir.Replace("'", "''");
        var escapedExe = exePath.Replace("'", "''");
        var escapedTemp = tempRoot.Replace("'", "''");
        var pid = Environment.ProcessId;

        var script = $$"""
$ErrorActionPreference = 'Stop'
Wait-Process -Id {{pid}}
Start-Sleep -Milliseconds 500
New-Item -ItemType Directory -Force -Path '{{escapedBackup}}' | Out-Null
Copy-Item -Path '{{escapedApp}}\*' -Destination '{{escapedBackup}}' -Recurse -Force
try {
    Copy-Item -Path '{{escapedNew}}\*' -Destination '{{escapedApp}}' -Recurse -Force
    $p = Start-Process -FilePath '{{escapedExe}}' -PassThru
    Start-Sleep -Seconds 5
    if ($p.HasExited) { throw "Updated MazLauncher exited immediately with code $($p.ExitCode)." }
    Remove-Item -LiteralPath '{{escapedTemp}}' -Recurse -Force
} catch {
    Get-Process MazLauncher -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Copy-Item -Path '{{escapedBackup}}\*' -Destination '{{escapedApp}}' -Recurse -Force
    Start-Process -FilePath '{{escapedExe}}'
}
""";

        await File.WriteAllTextAsync(scriptPath, script);

        Process.Start(new ProcessStartInfo
        {
            FileName = "powershell.exe",
            Arguments = $"-NoProfile -ExecutionPolicy Bypass -File \"{scriptPath}\"",
            UseShellExecute = false,
            CreateNoWindow = true
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
