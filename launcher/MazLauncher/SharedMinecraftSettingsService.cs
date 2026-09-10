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

    private static readonly string SharedOptionsPath = Path.Combine(DataRoot, "shared", "minecraft-options.txt");

    // Keep installation-specific/resource-pack/server state isolated.
    private static readonly HashSet<string> ExcludedKeys = new(StringComparer.OrdinalIgnoreCase)
    {
        "resourcePacks",
        "incompatibleResourcePacks",
        "lastServer",
        "serverAddress"
    };

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
        }
        catch
        {
            // Best-effort only; never interfere with Minecraft shutdown.
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
