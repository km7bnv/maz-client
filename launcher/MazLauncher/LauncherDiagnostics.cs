using System.Runtime.InteropServices;
using System.Text;

namespace MazLauncher;

internal static class LauncherDiagnostics
{
    private static readonly string DataRoot = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "MazLauncher"
    );
    private static readonly string DiagnosticsRoot = Path.Combine(DataRoot, "diagnostics");
    private static readonly string LatestPath = Path.Combine(DiagnosticsRoot, "latest.txt");

    public static void WriteLatest()
    {
        try
        {
            Directory.CreateDirectory(DiagnosticsRoot);
            var lines = new[]
            {
                "MazLauncher diagnostics (sanitized)",
                $"GeneratedUtc={DateTimeOffset.UtcNow:O}",
                $"MazLauncher={CloudUpdateService.CurrentLauncherVersion}",
                $"Minecraft={LauncherService.MinecraftVersion}",
                $"FabricLoader={LauncherService.FabricLoaderVersion}",
                $"OS={RuntimeInformation.OSDescription}",
                $"OSArchitecture={RuntimeInformation.OSArchitecture}",
                $"ProcessArchitecture={RuntimeInformation.ProcessArchitecture}",
                $".NET={Environment.Version}",
                $"TotalPhysicalMemoryMb={SystemMemoryInfo.GetTotalPhysicalMemoryMb()}",
                $"AppDataDriveFreeMb={GetAppDataDriveFreeMb()}",
                $"SettingsPresent={File.Exists(Path.Combine(DataRoot, "settings.json"))}",
                $"CachePresent={Directory.Exists(Path.Combine(DataRoot, "cache"))}",
                $"InstallationsPresent={Directory.Exists(Path.Combine(DataRoot, "installations"))}",
                $"LogsPresent={Directory.Exists(Path.Combine(DataRoot, "logs"))}",
                "Privacy=No account names, tokens, server addresses, full user paths, or file contents are recorded."
            };

            var temp = LatestPath + ".tmp";
            File.WriteAllLines(temp, lines, Encoding.UTF8);
            File.Move(temp, LatestPath, true);
        }
        catch
        {
            // Diagnostics must never block launcher startup.
        }
    }

    private static long GetAppDataDriveFreeMb()
    {
        try
        {
            var root = Path.GetPathRoot(DataRoot);
            if (string.IsNullOrWhiteSpace(root)) return -1;
            return new DriveInfo(root).AvailableFreeSpace / (1024L * 1024L);
        }
        catch
        {
            return -1;
        }
    }
}
