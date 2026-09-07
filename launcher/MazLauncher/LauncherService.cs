using System.IO;
using System.Net.Http;
using System.Text.Json;
using CmlLib.Core;
using CmlLib.Core.Auth;
using CmlLib.Core.Auth.Microsoft;
using CmlLib.Core.ProcessBuilder;

namespace MazLauncher;

public sealed class LauncherService
{
    public const string MinecraftVersion = "26.2";
    public const string FabricLoaderVersion = "0.19.5";

    private static readonly string DataRoot = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "MazLauncher"
    );

    private readonly HttpClient http = new();
    private readonly JELoginHandler loginHandler;
    private readonly CloudUpdateService cloudUpdates = new();

    public LauncherService()
    {
        Directory.CreateDirectory(DataRoot);
        Directory.CreateDirectory(Path.Combine(DataRoot, "cache"));

        loginHandler = new JELoginHandlerBuilder()
            .WithAccountManager(Path.Combine(DataRoot, "cache", "accounts.json"))
            .Build();

        http.DefaultRequestHeaders.UserAgent.ParseAdd("MazLauncher/1.0");
    }

    public async Task<MSession> SignInAsync()
    {
        return await loginHandler.Authenticate();
    }

    public async Task<MSession?> TryRestoreSessionAsync()
    {
        var account = loginHandler.AccountManager.GetAccounts().FirstOrDefault();
        if (account == null)
            return null;

        try
        {
            return await loginHandler.Authenticate(account);
        }
        catch
        {
            return null;
        }
    }

    public Task<CloudManifest?> GetCloudManifestAsync() => cloudUpdates.GetManifestAsync();

    public async Task CheckForLauncherUpdateAsync()
    {
        if (await cloudUpdates.HasLauncherUpdateAsync())
            await cloudUpdates.DownloadAndApplyLauncherUpdateAsync();
    }

    public async Task LaunchVanillaAsync(MSession session, Action<string, int>? progress = null)
    {
        var gameDir = Path.Combine(DataRoot, "vanilla");
        Directory.CreateDirectory(gameDir);
        await LaunchAsync(gameDir, MinecraftVersion, session, progress);
    }

    public async Task LaunchMazAsync(MSession session, Action<string, int>? progress = null)
    {
        var gameDir = Path.Combine(DataRoot, "maz");
        Directory.CreateDirectory(gameDir);

        progress?.Invoke("Checking cached MazClient instance...", 5);
        await EnsureFabricProfileAsync(gameDir);

        progress?.Invoke("Checking Fabric API...", 15);
        await EnsureModrinthModAsync(gameDir, "fabric-api", "fabric-api-");

        progress?.Invoke("Checking Sodium...", 22);
        await EnsureModrinthModAsync(gameDir, "sodium", "sodium-");

        progress?.Invoke("Checking Lithium...", 29);
        await EnsureModrinthModAsync(gameDir, "lithium", "lithium-");

        CleanupOldMazClientJars(gameDir);

        var cloudVersion = await cloudUpdates.EnsureMazClientCurrentAsync(gameDir, progress);
        if (cloudVersion == null)
        {
            progress?.Invoke("Cloud unavailable — using bundled MazClient...", 35);
            EnsureBundledMazClientJar(gameDir);
        }

        var fabricVersion = $"fabric-loader-{FabricLoaderVersion}-{MinecraftVersion}";
        await LaunchAsync(gameDir, fabricVersion, session, progress);
    }

    private async Task LaunchAsync(string gameDir, string version, MSession session, Action<string, int>? progress)
    {
        var launcher = new MinecraftLauncher(new MinecraftPath(gameDir));
        launcher.FileProgressChanged += (_, e) =>
        {
            var total = Math.Max(1, e.TotalTasks);
            var pct = 40 + (int)Math.Round((e.ProgressedTasks / (double)total) * 55.0);
            progress?.Invoke($"Checking {e.Name}...", Math.Clamp(pct, 40, 95));
        };

        progress?.Invoke($"Checking Minecraft {version} cache...", 40);
        var process = await launcher.InstallAndBuildProcessAsync(version, new MLaunchOption
        {
            Session = session,
            MaximumRamMb = 4096,
            MinimumRamMb = 1024
        });

        progress?.Invoke("Launching Minecraft...", 100);
        process.Start();
    }

    private async Task EnsureFabricProfileAsync(string gameDir)
    {
        var versionId = $"fabric-loader-{FabricLoaderVersion}-{MinecraftVersion}";
        var versionDir = Path.Combine(gameDir, "versions", versionId);
        var jsonPath = Path.Combine(versionDir, versionId + ".json");
        if (File.Exists(jsonPath))
            return;

        Directory.CreateDirectory(versionDir);
        var url = $"https://meta.fabricmc.net/v2/versions/loader/{MinecraftVersion}/{FabricLoaderVersion}/profile/json";
        var json = await http.GetStringAsync(url);
        await File.WriteAllTextAsync(jsonPath, json);
    }

    private async Task EnsureModrinthModAsync(string gameDir, string projectSlug, string filePrefix)
    {
        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);

        var query = $"https://api.modrinth.com/v2/project/{projectSlug}/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%22{MinecraftVersion}%22%5D";
        using var stream = await http.GetStreamAsync(query);
        using var doc = await JsonDocument.ParseAsync(stream);
        var versions = doc.RootElement;

        JsonElement? selectedVersion = null;
        foreach (var version in versions.EnumerateArray())
        {
            if (version.TryGetProperty("version_type", out var type) &&
                string.Equals(type.GetString(), "release", StringComparison.OrdinalIgnoreCase))
            {
                selectedVersion = version;
                break;
            }
        }

        if (selectedVersion == null)
            throw new InvalidOperationException($"No release build of {projectSlug} was found for Minecraft {MinecraftVersion} on Fabric.");

        var files = selectedVersion.Value.GetProperty("files");
        JsonElement selectedFile = files[0];
        foreach (var file in files.EnumerateArray())
        {
            if (file.TryGetProperty("primary", out var primary) && primary.GetBoolean())
            {
                selectedFile = file;
                break;
            }
        }

        var downloadUrl = selectedFile.GetProperty("url").GetString()!;
        var fileName = selectedFile.GetProperty("filename").GetString()!;
        var target = Path.Combine(modsDir, fileName);

        if (File.Exists(target))
        {
            DeleteOtherModVersions(modsDir, filePrefix, target);
            return;
        }

        var temp = target + ".download";
        try
        {
            await using var source = await http.GetStreamAsync(downloadUrl);
            await using var destination = File.Create(temp);
            await source.CopyToAsync(destination);
            File.Move(temp, target, true);
        }
        finally
        {
            if (File.Exists(temp))
                File.Delete(temp);
        }

        DeleteOtherModVersions(modsDir, filePrefix, target);
    }

    private static void DeleteOtherModVersions(string modsDir, string filePrefix, string keepPath)
    {
        foreach (var existing in Directory.EnumerateFiles(modsDir, filePrefix + "*.jar"))
        {
            if (!string.Equals(existing, keepPath, StringComparison.OrdinalIgnoreCase))
                File.Delete(existing);
        }
    }

    private static void CleanupOldMazClientJars(string gameDir)
    {
        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);
        var target = Path.Combine(modsDir, "maz-client.jar");

        foreach (var existing in Directory.EnumerateFiles(modsDir, "maz-client-*.jar"))
        {
            if (!string.Equals(existing, target, StringComparison.OrdinalIgnoreCase))
                File.Delete(existing);
        }
    }

    private static void EnsureBundledMazClientJar(string gameDir)
    {
        var target = Path.Combine(gameDir, "mods", "maz-client.jar");
        if (File.Exists(target))
            return;

        var bundled = Path.Combine(AppContext.BaseDirectory, "payload", "maz-client.jar");
        if (!File.Exists(bundled))
            throw new FileNotFoundException("MazClient payload is missing from the launcher installation.", bundled);

        File.Copy(bundled, target, true);
    }
}
