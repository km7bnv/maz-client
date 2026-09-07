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

    public async Task LaunchVanillaAsync(MSession session, Action<string, int>? progress = null)
    {
        var gameDir = Path.Combine(DataRoot, "vanilla");
        Directory.CreateDirectory(gameDir);
        await LaunchAsync(gameDir, MinecraftVersion, session, progress);
    }

    public async Task LaunchMazAsync(MSession session, Action<string, int>? progress = null)
    {
        // This directory is intentionally permanent. Minecraft options, worlds,
        // resource packs, screenshots and MazClient's config/HUD layout all live
        // here and survive launcher upgrades/restarts.
        var gameDir = Path.Combine(DataRoot, "maz");
        Directory.CreateDirectory(gameDir);

        progress?.Invoke("Checking cached MazClient instance...", 5);
        await EnsureFabricProfileAsync(gameDir);
        progress?.Invoke("Checking Fabric API...", 20);
        await EnsureFabricApiAsync(gameDir);
        progress?.Invoke("Checking MazClient...", 35);
        EnsureMazClientJar(gameDir);

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

    private async Task EnsureFabricApiAsync(string gameDir)
    {
        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);

        // Keep an already-downloaded Fabric API instead of redownloading it on
        // every launch. Cloud/update code can deliberately replace it later.
        if (Directory.EnumerateFiles(modsDir, "fabric-api-*.jar").Any())
            return;

        var query = "https://api.modrinth.com/v2/project/fabric-api/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%2226.2%22%5D";
        using var stream = await http.GetStreamAsync(query);
        using var doc = await JsonDocument.ParseAsync(stream);
        var versions = doc.RootElement;
        if (versions.GetArrayLength() == 0)
            throw new InvalidOperationException("No Fabric API build was found for Minecraft 26.2.");

        var files = versions[0].GetProperty("files");
        JsonElement selected = files[0];
        foreach (var file in files.EnumerateArray())
        {
            if (file.TryGetProperty("primary", out var primary) && primary.GetBoolean())
            {
                selected = file;
                break;
            }
        }

        var downloadUrl = selected.GetProperty("url").GetString()!;
        var fileName = selected.GetProperty("filename").GetString()!;
        var target = Path.Combine(modsDir, fileName);

        await using var source = await http.GetStreamAsync(downloadUrl);
        await using var destination = File.Create(target);
        await source.CopyToAsync(destination);
    }

    private static void EnsureMazClientJar(string gameDir)
    {
        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);
        var target = Path.Combine(modsDir, "maz-client.jar");

        // Keep the installed/cloud-updated JAR if one is already cached.
        if (File.Exists(target))
            return;

        var bundled = Path.Combine(AppContext.BaseDirectory, "payload", "maz-client.jar");
        if (!File.Exists(bundled))
            throw new FileNotFoundException("MazClient payload is missing from the launcher installation.", bundled);

        File.Copy(bundled, target, true);
    }
}
