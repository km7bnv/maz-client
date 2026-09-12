using System.IO;

namespace MazLauncher;

public partial class MainWindow
{
    // Runs when the launcher window is created. BetterF3 and SmoothHud used to
    // be managed by MazLauncher, so old cached installations may still contain
    // their JARs, markers, or config files after the integrations are removed.
    private readonly bool retiredManagedModCleanupComplete = CleanupRetiredManagedMods();

    private static bool CleanupRetiredManagedMods()
    {
        try
        {
            var dataRoot = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                "MazLauncher");
            var mazRoot = Path.Combine(dataRoot, "installations", "mazclient");

            if (!Directory.Exists(mazRoot)) return true;

            foreach (var modsDir in Directory.EnumerateDirectories(mazRoot, "mods", SearchOption.AllDirectories))
            {
                foreach (var file in Directory.EnumerateFiles(modsDir))
                {
                    var name = Path.GetFileName(file);
                    if (name.StartsWith("BetterF3", StringComparison.OrdinalIgnoreCase)
                        || name.StartsWith("betterf3", StringComparison.OrdinalIgnoreCase)
                        || IsSmoothHudArtifact(name)
                        || string.Equals(name, ".maz-betterf3", StringComparison.OrdinalIgnoreCase)
                        || string.Equals(name, ".maz-smoothhud", StringComparison.OrdinalIgnoreCase))
                    {
                        TryDelete(file);
                    }
                }
            }

            foreach (var configDir in Directory.EnumerateDirectories(mazRoot, "config", SearchOption.AllDirectories))
            {
                foreach (var file in Directory.EnumerateFiles(configDir, "*", SearchOption.AllDirectories))
                {
                    if (IsSmoothHudArtifact(Path.GetFileName(file))) TryDelete(file);
                }
            }
        }
        catch
        {
            // Cleanup must never stop MazLauncher from starting.
        }

        return true;
    }

    private static bool IsSmoothHudArtifact(string name)
    {
        return name.StartsWith("SmoothHud", StringComparison.OrdinalIgnoreCase)
            || name.StartsWith("smoothhud", StringComparison.OrdinalIgnoreCase)
            || name.StartsWith("smooth-hud", StringComparison.OrdinalIgnoreCase);
    }

    private static void TryDelete(string path)
    {
        try { if (File.Exists(path)) File.Delete(path); }
        catch { }
    }
}
