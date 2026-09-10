using System.IO;

namespace MazLauncher;

/// <summary>
/// Shares normal vanilla Minecraft options between MazLauncher-managed Vanilla and MazClient
/// installations without sharing mods, resource packs, worlds, servers or MazClient config.
/// </summary>
public static class SharedMinecraftSettingsService
{
    private static readonly string DataRoot = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "MazLauncher");

    private static readonly string InstallationsRoot = Path.Combine(DataRoot, "installations");
    private static readonly string SharedOptionsPath = Path.Combine(DataRoot, "shared", "minecraft-options.txt");

    private static readonly HashSet<string> ExcludedKeys = new(StringComparer.OrdinalIgnoreCase)
    {
        "resourcePacks",
        "incompatibleResourcePacks",
        "lastServer",
        "serverAddress"
    };

    public static void SynchronizeAllManagedInstallations()
    {
        try
        {
            CaptureNewestManagedOptions();
            if (!Directory.Exists(InstallationsRoot) || !File.Exists(SharedOptionsPath)) return;

            foreach (var optionsPath in Directory.EnumerateFiles(InstallationsRoot, "options.txt", SearchOption.AllDirectories))
            {
                var gameDir = Path.GetDirectoryName(optionsPath);
                if (!string.IsNullOrWhiteSpace(gameDir)) ApplyToInstallation(gameDir);
            }
        }
        catch
        {
            // Settings sync must never block MazLauncher startup.
        }
    }

    public static void PrepareForLaunch(string gameDir)
    {
        try
        {
            CaptureNewestManagedOptions();
            ApplyToInstallation(gameDir);
        }
        catch
        {
            // Settings sync must never block Minecraft from launching.
        }
    }

    private static void CaptureNewestManagedOptions()
    {
        if (!Directory.Exists(InstallationsRoot)) return;

        var newest = Directory.EnumerateFiles(InstallationsRoot, "options.txt", SearchOption.AllDirectories)
            .Select(path => new FileInfo(path))
            .Where(info => info.Exists)
            .OrderByDescending(info => info.LastWriteTimeUtc)
            .FirstOrDefault();

        if (newest == null) return;

        var sharedWrite = File.Exists(SharedOptionsPath)
            ? File.GetLastWriteTimeUtc(SharedOptionsPath)
            : DateTime.MinValue;

        if (newest.LastWriteTimeUtc > sharedWrite)
            CaptureFromInstallation(newest.DirectoryName!);
    }

    public static void ApplyToInstallation(string gameDir)
    {
        try
        {
            var target = Path.Combine(gameDir, "options.txt");
            var shared = ReadOptions(SharedOptionsPath, includeExcluded: false);
            if (shared.Count == 0)
            {
                CaptureFromInstallation(gameDir);
                return;
            }

            var localLines = File.Exists(target) ? File.ReadAllLines(target).ToList() : new List<string>();
            var local = ParseLines(localLines);

            foreach (var (key, value) in shared)
            {
                if (!ExcludedKeys.Contains(key)) local[key] = value;
            }

            WriteOptionsAtomic(target, local);
        }
        catch
        {
            // Settings sync must never block Minecraft from launching.
        }
    }

    public static void CaptureFromInstallation(string gameDir)
    {
        try
        {
            var source = Path.Combine(gameDir, "options.txt");
            if (!File.Exists(source)) return;

            var options = ReadOptions(source, includeExcluded: false);
            if (options.Count == 0) return;

            Directory.CreateDirectory(Path.GetDirectoryName(SharedOptionsPath)!);
            WriteOptionsAtomic(SharedOptionsPath, options);
            File.SetLastWriteTimeUtc(SharedOptionsPath, File.GetLastWriteTimeUtc(source));
        }
        catch
        {
            // Best-effort only; never interfere with Minecraft shutdown/launch.
        }
    }

    private static Dictionary<string, string> ReadOptions(string path, bool includeExcluded)
    {
        if (!File.Exists(path)) return new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        var parsed = ParseLines(File.ReadAllLines(path));
        if (includeExcluded) return parsed;

        foreach (var key in ExcludedKeys) parsed.Remove(key);
        return parsed;
    }

    private static Dictionary<string, string> ParseLines(IEnumerable<string> lines)
    {
        var result = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        foreach (var line in lines)
        {
            var split = line.IndexOf(':');
            if (split <= 0) continue;
            var key = line[..split].Trim();
            if (key.Length == 0) continue;
            result[key] = line[(split + 1)..];
        }
        return result;
    }

    private static void WriteOptionsAtomic(string path, IReadOnlyDictionary<string, string> options)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(path)!);
        var temp = path + "." + Guid.NewGuid().ToString("N") + ".tmp";
        try
        {
            File.WriteAllLines(temp, options.Select(pair => pair.Key + ":" + pair.Value));
            File.Move(temp, path, true);
        }
        finally
        {
            if (File.Exists(temp)) File.Delete(temp);
        }
    }
}
