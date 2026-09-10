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
    private static readonly string CacheRoot = Path.Combine(DataRoot, "cache");
    private static readonly string VanillaCatalogCache = Path.Combine(CacheRoot, "vanilla-versions.txt");
    private static readonly string MazCatalogCache = Path.Combine(CacheRoot, "mazclient-versions.txt");
    private static readonly string LatestVanillaMarker = Path.Combine(CacheRoot, "latest-vanilla-cached.txt");
    private static readonly string LatestMazMarker = Path.Combine(CacheRoot, "latest-mazclient-cached.txt");

    private readonly HttpClient http = new();
    private readonly JELoginHandler loginHandler;
    private readonly CloudUpdateService cloudUpdates = new();

    public LauncherService()
    {
        Directory.CreateDirectory(DataRoot);
        Directory.CreateDirectory(CacheRoot);
        Directory.CreateDirectory(Path.Combine(DataRoot, "installations"));

        loginHandler = new JELoginHandlerBuilder()
            .WithAccountManager(Path.Combine(CacheRoot, "accounts.json"))
            .Build();

        http.DefaultRequestHeaders.UserAgent.ParseAdd($"MazLauncher/{CloudUpdateService.CurrentLauncherVersion}");
    }

    public async Task<MSession> SignInAsync() => await loginHandler.Authenticate();

    public async Task SignOutAsync() => await loginHandler.SignoutWithBrowser();

    public async Task<MSession?> TryRestoreSessionAsync()
    {
        var account = loginHandler.AccountManager.GetAccounts().FirstOrDefault();
        if (account == null) return null;
        try { return await loginHandler.Authenticate(account); }
        catch { return null; }
    }

    public Task<CloudManifest?> GetCloudManifestAsync() => cloudUpdates.GetManifestAsync();

    public async Task CheckForLauncherUpdateAsync()
    {
        if (await cloudUpdates.HasLauncherUpdateAsync())
            await cloudUpdates.DownloadAndApplyLauncherUpdateAsync();
    }

    public async Task<IReadOnlyList<string>> GetVanillaVersionsAsync()
    {
        try
        {
            var catalogPath = Path.Combine(DataRoot, "catalog");
            Directory.CreateDirectory(catalogPath);
            var launcher = new MinecraftLauncher(new MinecraftPath(catalogPath));
            var versions = (await launcher.GetAllVersionsAsync())
                .Select(v => v.Name)
                .Where(name => !string.IsNullOrWhiteSpace(name) && !name.StartsWith("fabric-loader-", StringComparison.OrdinalIgnoreCase))
                .Distinct(StringComparer.OrdinalIgnoreCase)
                .ToList();
            await WriteVersionCacheAsync(VanillaCatalogCache, versions);
            return versions;
        }
        catch
        {
            var cached = ReadVersionCache(VanillaCatalogCache);
            if (cached.Count > 0) return cached;
            return new[] { MinecraftVersion };
        }
    }

    public async Task<IReadOnlyList<string>> GetMazClientVersionsAsync()
    {
        try
        {
            using var request = new HttpRequestMessage(HttpMethod.Get, "https://api.github.com/repos/km7bnv/maz-client/releases?per_page=100");
            request.Headers.Accept.ParseAdd("application/vnd.github+json");
            using var response = await http.SendAsync(request);
            response.EnsureSuccessStatusCode();
            using var stream = await response.Content.ReadAsStreamAsync();
            using var doc = await JsonDocument.ParseAsync(stream);

            var versions = new List<string>();
            foreach (var release in doc.RootElement.EnumerateArray())
            {
                if (release.TryGetProperty("draft", out var draft) && draft.GetBoolean()) continue;
                if (!release.TryGetProperty("tag_name", out var tagElement)) continue;
                var tag = tagElement.GetString();
                if (string.IsNullOrWhiteSpace(tag) || !tag.StartsWith("v", StringComparison.OrdinalIgnoreCase)) continue;
                var version = tag[1..];
                if (Version.TryParse(version, out _)) versions.Add(version);
            }

            var ordered = versions
                .Distinct(StringComparer.OrdinalIgnoreCase)
                .OrderByDescending(v => Version.TryParse(v, out var parsed) ? parsed : new Version(0, 0))
                .ToList();
            await WriteVersionCacheAsync(MazCatalogCache, ordered);
            return ordered;
        }
        catch
        {
            var cached = ReadVersionCache(MazCatalogCache);
            if (cached.Count > 0) return cached;
            var manifest = await GetCloudManifestAsync();
            if (manifest != null && Version.TryParse(manifest.MazClientVersion, out _))
                return new[] { manifest.MazClientVersion };
            return Array.Empty<string>();
        }
    }

    public async Task WarmLatestOfflineCacheAsync(MSession session, Action<string, int>? progress = null)
    {
        var vanillaDir = Path.Combine(DataRoot, "installations", "vanilla", SafeName(MinecraftVersion));
        var vanillaMarker = await ReadMarkerAsync(LatestVanillaMarker);
        var vanillaCurrent = string.Equals(vanillaMarker, MinecraftVersion, StringComparison.OrdinalIgnoreCase)
                             && Directory.Exists(vanillaDir)
                             && Directory.Exists(Path.Combine(vanillaDir, "versions"));

        if (vanillaCurrent)
        {
            progress?.Invoke($"Vanilla {MinecraftVersion} cache already current — skipping", 20);
        }
        else
        {
            progress?.Invoke($"Vanilla cache needs refresh — caching {MinecraftVersion}...", 5);
            Directory.CreateDirectory(vanillaDir);
            await PrepareVersionAsync(vanillaDir, MinecraftVersion, session, progress);
            await File.WriteAllTextAsync(LatestVanillaMarker, MinecraftVersion);
            progress?.Invoke($"Vanilla {MinecraftVersion} cache refreshed", 45);
        }

        progress?.Invoke("Checking latest MazClient cache version...", 50);
        var mazVersions = await GetMazClientVersionsAsync();
        var latestMaz = mazVersions.FirstOrDefault();
        if (string.IsNullOrWhiteSpace(latestMaz))
        {
            progress?.Invoke("MazClient version unavailable — keeping existing cache", 100);
            return;
        }

        var mazDir = GetMazInstallationDirectory(latestMaz);
        var modsDir = Path.Combine(mazDir, "mods");
        var mazMarker = await ReadMarkerAsync(LatestMazMarker);
        var fabricVersion = $"fabric-loader-{FabricLoaderVersion}-{MinecraftVersion}";
        var fabricProfile = Path.Combine(mazDir, "versions", fabricVersion, fabricVersion + ".json");
        var mazJar = Path.Combine(modsDir, "maz-client.jar");
        var mazVersionMarker = Path.Combine(modsDir, ".mazclient-version");
        var installedMazVersion = await ReadMarkerAsync(mazVersionMarker);
        var voiceChatPresent = HasManagedMod(modsDir, "voicechat-");
        var immediatelyFastPresent = HasManagedMod(modsDir, "ImmediatelyFast-");
        var entityCullingPresent = HasManagedMod(modsDir, "entityculling-");
        var ferriteCorePresent = HasManagedMod(modsDir, "ferritecore-");
        var mazCurrent = string.Equals(mazMarker, latestMaz, StringComparison.OrdinalIgnoreCase)
                         && string.Equals(installedMazVersion, latestMaz, StringComparison.OrdinalIgnoreCase)
                         && File.Exists(mazJar)
                         && File.Exists(fabricProfile)
                         && voiceChatPresent
                         && immediatelyFastPresent
                         && entityCullingPresent
                         && ferriteCorePresent;

        if (mazCurrent)
        {
            progress?.Invoke($"MazClient {latestMaz} cache already current — skipping", 100);
            return;
        }

        progress?.Invoke($"MazClient cache needs refresh — caching {latestMaz}...", 55);
        Directory.CreateDirectory(mazDir);
        await EnsureFabricProfileAsync(mazDir, MinecraftVersion);
        await EnsureModrinthModAsync(mazDir, "fabric-api", "fabric-api-", MinecraftVersion);
        await EnsureModrinthModAsync(mazDir, "sodium", "sodium-", MinecraftVersion);
        await EnsureModrinthModAsync(mazDir, "lithium", "lithium-", MinecraftVersion);
        await EnsureModrinthModAsync(mazDir, "simple-voice-chat", "voicechat-", MinecraftVersion);
        await EnsureModrinthModAsync(mazDir, "immediatelyfast", "ImmediatelyFast-", MinecraftVersion);
        await EnsureModrinthModAsync(mazDir, "entityculling", "entityculling-", MinecraftVersion);
        await EnsureModrinthModAsync(mazDir, "ferrite-core", "ferritecore-", MinecraftVersion);
        CleanupOldMazClientJars(mazDir);
        await EnsureMazClientVersionAsync(mazDir, latestMaz, progress);
        await PrepareVersionAsync(mazDir, fabricVersion, session, progress);
        await File.WriteAllTextAsync(LatestMazMarker, latestMaz);
        progress?.Invoke($"Offline cache ready: Vanilla {MinecraftVersion} + MazClient {latestMaz}", 100);
    }

    public Task LaunchVanillaAsync(MSession session, Action<string, int>? progress = null) =>
        LaunchVanillaAsync(session, MinecraftVersion, progress);

    public async Task LaunchVanillaAsync(MSession session, string version, Action<string, int>? progress = null)
    {
        if (string.IsNullOrWhiteSpace(version)) throw new ArgumentException("Choose a Vanilla Minecraft version first.", nameof(version));
        var gameDir = Path.Combine(DataRoot, "installations", "vanilla", SafeName(version));
        Directory.CreateDirectory(gameDir);

        progress?.Invoke("Verifying Vanilla isolation...", 5);
        VanillaPurityGuard.CleanManagedArtifacts(gameDir);
        var contamination = VanillaPurityGuard.FindManagedArtifacts(gameDir);
        if (contamination.Count > 0)
        {
            throw new InvalidOperationException(
                "Vanilla Purity Guard blocked launch because MazClient-managed artifacts remain in the Vanilla installation: "
                + string.Join(", ", contamination));
        }

        await LaunchAsync(gameDir, version, session, progress);
    }

    public Task LaunchMazAsync(MSession session, Action<string, int>? progress = null) =>
        LaunchMazLatestAsync(session, progress);

    private async Task LaunchMazLatestAsync(MSession session, Action<string, int>? progress)
    {
        var manifest = await GetCloudManifestAsync();
        if (manifest == null) throw new InvalidOperationException("MazClient version list is unavailable.");
        await LaunchMazAsync(session, manifest.MazClientVersion, progress);
    }

    public async Task LaunchMazAsync(MSession session, string mazClientVersion, Action<string, int>? progress = null)
    {
        if (string.IsNullOrWhiteSpace(mazClientVersion)) throw new ArgumentException("Choose a MazClient version first.", nameof(mazClientVersion));
        var gameDir = GetMazInstallationDirectory(mazClientVersion);
        Directory.CreateDirectory(gameDir);

        progress?.Invoke($"Preparing MazClient {mazClientVersion}...", 5);
        await EnsureFabricProfileAsync(gameDir, MinecraftVersion);

        progress?.Invoke("Checking Fabric API...", 15);
        await EnsureModrinthModAsync(gameDir, "fabric-api", "fabric-api-", MinecraftVersion);
        progress?.Invoke("Checking Sodium...", 22);
        await EnsureModrinthModAsync(gameDir, "sodium", "sodium-", MinecraftVersion);
        progress?.Invoke("Checking Lithium...", 29);
        await EnsureModrinthModAsync(gameDir, "lithium", "lithium-", MinecraftVersion);
        progress?.Invoke("Checking Simple Voice Chat...", 34);
        await EnsureModrinthModAsync(gameDir, "simple-voice-chat", "voicechat-", MinecraftVersion);
        progress?.Invoke("Checking ImmediatelyFast...", 37);
        await EnsureModrinthModAsync(gameDir, "immediatelyfast", "ImmediatelyFast-", MinecraftVersion);
        progress?.Invoke("Checking Entity Culling...", 39);
        await EnsureModrinthModAsync(gameDir, "entityculling", "entityculling-", MinecraftVersion);
        progress?.Invoke("Checking FerriteCore...", 41);
        await EnsureModrinthModAsync(gameDir, "ferrite-core", "ferritecore-", MinecraftVersion);

        CleanupOldMazClientJars(gameDir);
        await EnsureMazClientVersionAsync(gameDir, mazClientVersion, progress);

        var fabricVersion = $"fabric-loader-{FabricLoaderVersion}-{MinecraftVersion}";
        await LaunchAsync(gameDir, fabricVersion, session, progress);
    }

    public string GetMazModsDirectory(string mazClientVersion)
    {
        var mods = Path.Combine(GetMazInstallationDirectory(mazClientVersion), "mods");
        Directory.CreateDirectory(mods);
        return mods;
    }

    public IReadOnlyList<string> GetInstalledMods(string mazClientVersion)
    {
        var dir = GetMazModsDirectory(mazClientVersion);
        return Directory.EnumerateFiles(dir)
            .Where(path => path.EndsWith(".jar", StringComparison.OrdinalIgnoreCase) || path.EndsWith(".jar.disabled", StringComparison.OrdinalIgnoreCase))
            .Select(Path.GetFileName)
            .Where(name => name != null)
            .Cast<string>()
            .OrderBy(name => name, StringComparer.OrdinalIgnoreCase)
            .ToList();
    }

    public void AddMod(string mazClientVersion, string sourcePath)
    {
        if (!File.Exists(sourcePath)) throw new FileNotFoundException("Mod JAR not found.", sourcePath);
        if (!sourcePath.EndsWith(".jar", StringComparison.OrdinalIgnoreCase)) throw new InvalidOperationException("Only .jar mods can be added.");
        var target = Path.Combine(GetMazModsDirectory(mazClientVersion), Path.GetFileName(sourcePath));
        File.Copy(sourcePath, target, true);
    }

    public void ToggleMod(string mazClientVersion, string fileName)
    {
        if (IsManagedCoreMod(fileName)) throw new InvalidOperationException("MazClient, Fabric API, Sodium, Lithium, Simple Voice Chat, ImmediatelyFast, Entity Culling, and FerriteCore are managed by MazLauncher and cannot be disabled here.");
        var dir = GetMazModsDirectory(mazClientVersion);
        var source = Path.Combine(dir, fileName);
        if (!File.Exists(source)) return;
        var target = fileName.EndsWith(".disabled", StringComparison.OrdinalIgnoreCase)
            ? source[..^".disabled".Length]
            : source + ".disabled";
        File.Move(source, target, true);
    }

    public void RemoveMod(string mazClientVersion, string fileName)
    {
        if (IsManagedCoreMod(fileName)) throw new InvalidOperationException("MazClient, Fabric API, Sodium, Lithium, Simple Voice Chat, ImmediatelyFast, Entity Culling, and FerriteCore are managed by MazLauncher and cannot be removed here.");
        var path = Path.Combine(GetMazModsDirectory(mazClientVersion), fileName);
        if (File.Exists(path)) File.Delete(path);
    }

    private static bool IsManagedCoreMod(string fileName)
    {
        var name = fileName.ToLowerInvariant();
        return name.StartsWith("maz-client")
               || name.StartsWith("fabric-api-")
               || name.StartsWith("sodium-")
               || name.StartsWith("lithium-")
               || name.StartsWith("voicechat-")
               || name.StartsWith("immediatelyfast-")
               || name.StartsWith("entityculling-")
               || name.StartsWith("ferritecore-");
    }

    private static bool HasManagedMod(string modsDir, string filePrefix)
    {
        if (!Directory.Exists(modsDir)) return false;
        return Directory.EnumerateFiles(modsDir)
            .Select(Path.GetFileName)
            .Where(name => name != null)
            .Any(name => name!.StartsWith(filePrefix, StringComparison.OrdinalIgnoreCase)
                         && name.EndsWith(".jar", StringComparison.OrdinalIgnoreCase));
    }

    private static string GetMazInstallationDirectory(string version) =>
        Path.Combine(DataRoot, "installations", "mazclient", SafeName(version));

    private static string SafeName(string value)
    {
        var invalid = Path.GetInvalidFileNameChars();
        return new string(value.Select(ch => invalid.Contains(ch) ? '_' : ch).ToArray());
    }

    private static async Task<string> ReadMarkerAsync(string path)
    {
        if (!File.Exists(path)) return string.Empty;
        try { return (await File.ReadAllTextAsync(path)).Trim(); }
        catch { return string.Empty; }
    }

    private static (int MinimumRamMb, int MaximumRamMb) GetConfiguredMemory()
    {
        var settings = LauncherPreferences.Load();
        settings.NormalizeMemory();
        return (settings.MinimumRamMb, settings.MaximumRamMb);
    }

    private async Task PrepareVersionAsync(string gameDir, string version, MSession session, Action<string, int>? progress)
    {
        var launcher = new MinecraftLauncher(new MinecraftPath(gameDir));
        launcher.FileProgressChanged += (_, e) =>
        {
            var total = Math.Max(1, e.TotalTasks);
            var pct = 10 + (int)Math.Round((e.ProgressedTasks / (double)total) * 80.0);
            progress?.Invoke($"Caching {e.Name}...", Math.Clamp(pct, 10, 90));
        };
        var memory = GetConfiguredMemory();
        await launcher.InstallAndBuildProcessAsync(version, new MLaunchOption
        {
            Session = session,
            MaximumRamMb = memory.MaximumRamMb,
            MinimumRamMb = memory.MinimumRamMb
        });
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
        var memory = GetConfiguredMemory();
        var process = await launcher.InstallAndBuildProcessAsync(version, new MLaunchOption
        {
            Session = session,
            MaximumRamMb = memory.MaximumRamMb,
            MinimumRamMb = memory.MinimumRamMb
        });
        progress?.Invoke($"Launching Minecraft with {memory.MinimumRamMb}–{memory.MaximumRamMb} MB RAM...", 100);
        process.Start();
    }

    private async Task EnsureFabricProfileAsync(string gameDir, string minecraftVersion)
    {
        var versionId = $"fabric-loader-{FabricLoaderVersion}-{minecraftVersion}";
        var versionDir = Path.Combine(gameDir, "versions", versionId);
        var jsonPath = Path.Combine(versionDir, versionId + ".json");
        if (File.Exists(jsonPath)) return;
        Directory.CreateDirectory(versionDir);
        var url = $"https://meta.fabricmc.net/v2/versions/loader/{minecraftVersion}/{FabricLoaderVersion}/profile/json";
        var json = await http.GetStringAsync(url);
        await File.WriteAllTextAsync(jsonPath, json);
    }

    private async Task EnsureMazClientVersionAsync(string gameDir, string version, Action<string, int>? progress)
    {
        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);
        var target = Path.Combine(modsDir, "maz-client.jar");
        var marker = Path.Combine(modsDir, ".mazclient-version");
        if (File.Exists(target) && File.Exists(marker) && string.Equals((await File.ReadAllTextAsync(marker)).Trim(), version, StringComparison.OrdinalIgnoreCase)) return;

        var manifest = await GetCloudManifestAsync();
        if (manifest != null && string.Equals(version, manifest.MazClientVersion, StringComparison.OrdinalIgnoreCase))
        {
            progress?.Invoke($"Downloading MazClient {version} from cloud...", 35);
            var installedVersion = await cloudUpdates.EnsureMazClientCurrentAsync(gameDir, progress);
            if (!string.Equals(installedVersion, version, StringComparison.OrdinalIgnoreCase) || !File.Exists(target))
                throw new InvalidOperationException($"MazClient {version} could not be downloaded from the cloud package channel.");

            await File.WriteAllTextAsync(marker, version);
            return;
        }

        progress?.Invoke($"Downloading legacy MazClient {version}...", 35);
        var url = $"https://github.com/km7bnv/maz-client/releases/download/v{version}/maz-client-{version}.jar";
        var temp = target + "." + Guid.NewGuid().ToString("N") + ".download";
        try
        {
            using var response = await http.GetAsync(url, HttpCompletionOption.ResponseHeadersRead);
            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException($"MazClient {version} is not available from the legacy package source. Choose the latest MazClient version or reinstall/update MazLauncher.");

            await using (var source = await response.Content.ReadAsStreamAsync())
            await using (var destination = new FileStream(temp, FileMode.CreateNew, FileAccess.Write, FileShare.None))
            {
                await source.CopyToAsync(destination);
                await destination.FlushAsync();
            }
            if (new FileInfo(temp).Length < 10_000) throw new InvalidDataException("Downloaded MazClient JAR is unexpectedly small.");
            File.Move(temp, target, true);
            await File.WriteAllTextAsync(marker, version);
        }
        finally
        {
            if (File.Exists(temp)) File.Delete(temp);
        }
    }

    private async Task EnsureModrinthModAsync(string gameDir, string projectSlug, string filePrefix, string minecraftVersion)
    {
        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);
        var existing = Directory.EnumerateFiles(modsDir)
            .FirstOrDefault(path => Path.GetFileName(path).StartsWith(filePrefix, StringComparison.OrdinalIgnoreCase)
                                    && path.EndsWith(".jar", StringComparison.OrdinalIgnoreCase));
        try
        {
            var query = $"https://api.modrinth.com/v2/project/{projectSlug}/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%22{minecraftVersion}%22%5D";
            using var stream = await http.GetStreamAsync(query);
            using var doc = await JsonDocument.ParseAsync(stream);
            var versions = doc.RootElement;
            JsonElement? selectedVersion = null;
            foreach (var version in versions.EnumerateArray())
            {
                if (version.TryGetProperty("version_type", out var type) && string.Equals(type.GetString(), "release", StringComparison.OrdinalIgnoreCase)) { selectedVersion = version; break; }
            }
            if (selectedVersion == null) throw new InvalidOperationException($"No release build of {projectSlug} was found for Minecraft {minecraftVersion} on Fabric.");
            var files = selectedVersion.Value.GetProperty("files");
            JsonElement selectedFile = files[0];
            foreach (var file in files.EnumerateArray()) if (file.TryGetProperty("primary", out var primary) && primary.GetBoolean()) { selectedFile = file; break; }
            var downloadUrl = selectedFile.GetProperty("url").GetString()!;
            var fileName = selectedFile.GetProperty("filename").GetString()!;
            var target = Path.Combine(modsDir, fileName);
            if (File.Exists(target)) { DeleteOtherModVersions(modsDir, filePrefix, target); return; }
            var temp = target + "." + Guid.NewGuid().ToString("N") + ".download";
            try
            {
                await using (var source = await http.GetStreamAsync(downloadUrl))
                await using (var destination = new FileStream(temp, FileMode.CreateNew, FileAccess.Write, FileShare.None))
                { await source.CopyToAsync(destination); await destination.FlushAsync(); }
                File.Move(temp, target, true);
            }
            finally { if (File.Exists(temp)) File.Delete(temp); }
            DeleteOtherModVersions(modsDir, filePrefix, target);
        }
        catch when (existing != null && File.Exists(existing))
        {
            // Offline: keep the last successfully cached managed mod instead of blocking launch.
        }
    }

    private static async Task WriteVersionCacheAsync(string path, IEnumerable<string> versions)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(path)!);
        await File.WriteAllLinesAsync(path, versions.Where(v => !string.IsNullOrWhiteSpace(v)));
    }

    private static IReadOnlyList<string> ReadVersionCache(string path)
    {
        if (!File.Exists(path)) return Array.Empty<string>();
        return File.ReadAllLines(path)
            .Select(v => v.Trim())
            .Where(v => !string.IsNullOrWhiteSpace(v))
            .Distinct(StringComparer.OrdinalIgnoreCase)
            .ToList();
    }

    private static void DeleteOtherModVersions(string modsDir, string filePrefix, string keepPath)
    {
        foreach (var existing in Directory.EnumerateFiles(modsDir)
                     .Where(path => Path.GetFileName(path).StartsWith(filePrefix, StringComparison.OrdinalIgnoreCase)
                                    && path.EndsWith(".jar", StringComparison.OrdinalIgnoreCase)))
            if (!string.Equals(existing, keepPath, StringComparison.OrdinalIgnoreCase)) File.Delete(existing);
    }

    private static void CleanupOldMazClientJars(string gameDir)
    {
        var modsDir = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(modsDir);
        var target = Path.Combine(modsDir, "maz-client.jar");
        foreach (var existing in Directory.EnumerateFiles(modsDir, "maz-client-*.jar"))
            if (!string.Equals(existing, target, StringComparison.OrdinalIgnoreCase)) File.Delete(existing);
    }
}
