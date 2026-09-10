using System.IO;

namespace MazLauncher;

/// <summary>
/// Keeps launcher-managed Vanilla installations free of MazClient-managed mods.
/// Only artifacts owned by MazLauncher are removed; unrelated user files are preserved.
/// </summary>
public static class VanillaPurityGuard
{
    private static readonly string[] ManagedPrefixes =
    {
        "maz-client",
        "fabric-api-",
        "sodium-",
        "lithium-",
        "voicechat-",
        "ImmediatelyFast-",
        "entityculling-",
        "ferritecore-"
    };

    public static int CleanManagedArtifacts(string gameDir)
    {
        try
        {
            var modsDir = Path.Combine(gameDir, "mods");
            if (!Directory.Exists(modsDir)) return 0;

            var removed = 0;
            foreach (var path in Directory.EnumerateFiles(modsDir))
            {
                var name = Path.GetFileName(path);
                if (string.IsNullOrWhiteSpace(name)) continue;
                if (!IsManagedArtifact(name)) continue;

                File.Delete(path);
                removed++;
            }

            return removed;
        }
        catch
        {
            // The caller performs a second verification pass and blocks launch if contamination remains.
            return -1;
        }
    }

    public static IReadOnlyList<string> FindManagedArtifacts(string gameDir)
    {
        try
        {
            var modsDir = Path.Combine(gameDir, "mods");
            if (!Directory.Exists(modsDir)) return Array.Empty<string>();

            return Directory.EnumerateFiles(modsDir)
                .Select(Path.GetFileName)
                .Where(name => !string.IsNullOrWhiteSpace(name) && IsManagedArtifact(name!))
                .Cast<string>()
                .OrderBy(name => name, StringComparer.OrdinalIgnoreCase)
                .ToList();
        }
        catch
        {
            return new[] { "<verification unavailable>" };
        }
    }

    private static bool IsManagedArtifact(string fileName)
    {
        var candidate = fileName;
        if (!(candidate.EndsWith(".jar", StringComparison.OrdinalIgnoreCase)
              || candidate.EndsWith(".jar.disabled", StringComparison.OrdinalIgnoreCase)))
            return false;

        return ManagedPrefixes.Any(prefix => candidate.StartsWith(prefix, StringComparison.OrdinalIgnoreCase));
    }
}
