using System.Diagnostics;
using System.IO.Compression;
using System.Net.Http;
using System.Reflection;
using System.Text.Json;

namespace MazLauncher;

public sealed class CloudUpdateService
{
    private const string ManifestUrl = "https://github.com/km7bnv/maz-client/releases/download/cloud/latest.json";
    private const string LauncherZipUrl = "https://github.com/km7bnv/maz-client/releases/download/cloud/MazLauncher-win-x64.zip";

    private readonly HttpClient http = new();

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

    public async Task DownloadAndApplyLauncherUpdateAsync()
    {
        var tempRoot = Path.Combine(Path.GetTempPath(), "MazLauncherUpdate-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(tempRoot);
        var zipPath = Path.Combine(tempRoot, "launcher.zip");
        var extractDir = Path.Combine(tempRoot, "new");

        await using (var source = await http.GetStreamAsync(LauncherZipUrl))
        await using (var target = File.Create(zipPath))
            await source.CopyToAsync(target);

        ZipFile.ExtractToDirectory(zipPath, extractDir);

        var appDir = AppContext.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
        var exePath = Path.Combine(appDir, "MazLauncher.exe");
        var scriptPath = Path.Combine(tempRoot, "update.ps1");
        var escapedApp = appDir.Replace("'", "''");
        var escapedNew = extractDir.Replace("'", "''");
        var escapedExe = exePath.Replace("'", "''");
        var pid = Environment.ProcessId;

        var script = $"""
$ErrorActionPreference = 'Stop'
Wait-Process -Id {pid}
Start-Sleep -Milliseconds 500
Copy-Item -Path '{escapedNew}\*' -Destination '{escapedApp}' -Recurse -Force
Start-Process '{escapedExe}'
Remove-Item -LiteralPath $PSScriptRoot -Recurse -Force
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
    public string LauncherVersion { get; set; } = "0.0.0";
}
