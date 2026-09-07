using System.IO;
using System.Windows;
using System.Windows.Controls;

namespace MazLauncher;

public partial class MainWindow
{
    private bool mazPersistenceInstalled;

    private void InstallMazPersistenceMigration()
    {
        if (mazPersistenceInstalled) return;
        mazPersistenceInstalled = true;

        MazClientVersionBox.SelectionChanged += (_, _) =>
        {
            if (MazClientVersionBox.SelectedItem is string version)
                TryMigrateMazClientUserData(version);
        };

        if (MazClientVersionBox.SelectedItem is string current)
            TryMigrateMazClientUserData(current);
    }

    private void TryMigrateMazClientUserData(string targetVersion)
    {
        try
        {
            var dataRoot = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                "MazLauncher",
                "installations",
                "mazclient");
            Directory.CreateDirectory(dataRoot);

            var targetDir = Path.Combine(dataRoot, SafePersistenceName(targetVersion));
            var marker = Path.Combine(targetDir, ".mazclient-userdata-migrated");
            if (File.Exists(marker)) return;

            var previous = Directory.EnumerateDirectories(dataRoot)
                .Where(path => !string.Equals(path, targetDir, StringComparison.OrdinalIgnoreCase))
                .Select(path => new
                {
                    Path = path,
                    VersionText = Path.GetFileName(path),
                    Version = Version.TryParse(Path.GetFileName(path), out var v) ? v : null
                })
                .Where(item => item.Version != null)
                .OrderByDescending(item => item.Version)
                .FirstOrDefault();

            Directory.CreateDirectory(targetDir);
            if (previous == null)
            {
                File.WriteAllText(marker, "no-previous-version");
                AddLauncherLog($"MazClient {targetVersion}: no previous user data found to migrate");
                return;
            }

            AddLauncherLog($"MazClient {targetVersion}: migrating user data from {previous.VersionText}");

            CopyFileIfMissing(previous.Path, targetDir, "options.txt");
            CopyFileIfMissing(previous.Path, targetDir, "servers.dat");
            CopyFileIfMissing(previous.Path, targetDir, "servers.dat_old");
            CopyFileIfMissing(previous.Path, targetDir, "realms_persistence.json");
            CopyFileIfMissing(previous.Path, targetDir, "hotbar.nbt");

            CopyDirectoryIfMissing(Path.Combine(previous.Path, "config"), Path.Combine(targetDir, "config"));
            CopyDirectoryIfMissing(Path.Combine(previous.Path, "resourcepacks"), Path.Combine(targetDir, "resourcepacks"));
            CopyDirectoryIfMissing(Path.Combine(previous.Path, "shaderpacks"), Path.Combine(targetDir, "shaderpacks"));
            CopyDirectoryIfMissing(Path.Combine(previous.Path, "screenshots"), Path.Combine(targetDir, "screenshots"));
            CopyDirectoryIfMissing(Path.Combine(previous.Path, "saves"), Path.Combine(targetDir, "saves"));

            File.WriteAllText(marker, previous.VersionText);
            AddLauncherLog($"MazClient {targetVersion}: user data migration complete");
        }
        catch (Exception ex)
        {
            AddLauncherLog($"WARNING: MazClient user data migration failed: {ex.Message}");
        }
    }

    private static void CopyFileIfMissing(string sourceRoot, string targetRoot, string fileName)
    {
        var source = Path.Combine(sourceRoot, fileName);
        var target = Path.Combine(targetRoot, fileName);
        if (File.Exists(source) && !File.Exists(target))
        {
            Directory.CreateDirectory(Path.GetDirectoryName(target)!);
            File.Copy(source, target, false);
        }
    }

    private static void CopyDirectoryIfMissing(string source, string target)
    {
        if (!Directory.Exists(source) || Directory.Exists(target)) return;
        Directory.CreateDirectory(target);
        foreach (var directory in Directory.EnumerateDirectories(source, "*", SearchOption.AllDirectories))
        {
            var relative = Path.GetRelativePath(source, directory);
            Directory.CreateDirectory(Path.Combine(target, relative));
        }
        foreach (var file in Directory.EnumerateFiles(source, "*", SearchOption.AllDirectories))
        {
            var relative = Path.GetRelativePath(source, file);
            var destination = Path.Combine(target, relative);
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            File.Copy(file, destination, false);
        }
    }

    private static string SafePersistenceName(string value)
    {
        var invalid = Path.GetInvalidFileNameChars();
        return new string(value.Select(ch => invalid.Contains(ch) ? '_' : ch).ToArray());
    }
}
