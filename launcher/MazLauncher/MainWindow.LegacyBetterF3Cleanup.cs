using System.IO;

namespace MazLauncher;

public partial class MainWindow
{
    // Runs when the launcher window is created. BetterF3 used to be managed by
    // MazLauncher, so old cached installations may still contain its JAR and
    // marker even after the integration itself was removed.
    private readonly bool legacyBetterF3CleanupComplete = CleanupLegacyBetterF3();

    private static bool CleanupLegacyBetterF3()
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
                        || string.Equals(name, ".maz-betterf3", StringComparison.OrdinalIgnoreCase))
                    {
                        try { File.Delete(file); }
                        catch { }
                    }
                }
            }
        }
        catch
        {
            // Cleanup must never stop MazLauncher from starting.
        }

        return true;
    }
}
