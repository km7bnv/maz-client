using System.IO;

namespace MazLauncher;

/// <summary>
/// Shares only non-performance Minecraft preferences between MazLauncher-managed Vanilla and
/// MazClient installations. Rendering/performance options, mods, resource packs, worlds,
/// servers, and MazClient config remain isolated between the two modes.
/// </summary>
public static class SharedMinecraftSettingsService
{
    private static readonly string DataRoot = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "MazLauncher");

    private static readonly string InstallationsRoot = Path.Combine(DataRoot, "installations");
    private static readonly string SharedOptionsPath = Path.Combine(DataRoot, "shared", "minecraft-options.txt");

    // Deliberately use an allow-list instead of an exclusion-list. Minecraft adds new options
    // over time, and unknown future video/performance options must never leak from MazClient
    // into Vanilla just because MazLauncher does not know their names yet.
    private static readonly HashSet<string> SharedKeys = new(StringComparer.OrdinalIgnoreCase)
    {
        "lang",
        "mouseSensitivity",
        "invertYMouse",
        "discrete_mouse_scroll",
        "mouseWheelSensitivity",
        "touchscreen",
        "rawMouseInput",
        "autoJump",
        "toggleCrouch",
        "toggleSprint",
        "narrator",
        "chatVisibility",
        "chatColors",
        "chatLinks",
        "chatLinksPrompt",
        "chatOpacity",
        "textBackgroundOpacity",
        "backgroundForChatOnly",
        "hideServerAddress",
        "advancedItemTooltips",
        "pauseOnLostFocus",
        "showSubtitles",
        "directionalAudio",
        "notificationDisplayTime",
        "darkMojangStudiosBackground",
        "hideMatchedNames",
        "operatorItemsTab"
    };

    private static readonly string[] SharedPrefixes =
    {
        "key_",
        "soundCategory_",
        "modelPart_"
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
        {
            CaptureFromInstallation(newest.DirectoryName!);
            return;
        }

        // 0.6.20 could have persisted video/performance options in the shared snapshot. Rewrite
        // any existing snapshot through the new allow-list so stale performance keys cannot be
        // applied after upgrading to 0.6.21.
        SanitizeSharedSnapshot();
    }

    public static void ApplyToInstallation(string gameDir)
    {
        try
        {
            var target = Path.Combine(gameDir, "options.txt");
            var shared = ReadShareableOptions(SharedOptionsPath);
            if (shared.Count == 0)
            {
                CaptureFromInstallation(gameDir);
                return;
            }

            var localLines = File.Exists(target) ? File.ReadAllLines(target).ToList() : new List<string>();
            var local = ParseLines(localLines);

            foreach (var (key, value) in shared)
            {
                if (IsShareableKey(key)) local[key] = value;
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

            var options = ReadShareableOptions(source);
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

    private static void SanitizeSharedSnapshot()
    {
        if (!File.Exists(SharedOptionsPath)) return;
        var filtered = ReadShareableOptions(SharedOptionsPath);
        WriteOptionsAtomic(SharedOptionsPath, filtered);
    }

    private static Dictionary<string, string> ReadShareableOptions(string path)
    {
        if (!File.Exists(path)) return new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        return ParseLines(File.ReadAllLines(path))
            .Where(pair => IsShareableKey(pair.Key))
            .ToDictionary(pair => pair.Key, pair => pair.Value, StringComparer.OrdinalIgnoreCase);
    }

    private static bool IsShareableKey(string key)
    {
        if (SharedKeys.Contains(key)) return true;
        return SharedPrefixes.Any(prefix => key.StartsWith(prefix, StringComparison.OrdinalIgnoreCase));
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
