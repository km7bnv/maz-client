using System.IO;
using System.Text.Json;

namespace MazLauncher;

public sealed class LauncherPreferences
{
    private static readonly string DataRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher");
    private static readonly string SettingsPath = Path.Combine(DataRoot, "settings.json");
    private static readonly string SettingsBackupPath = SettingsPath + ".bak";
    private static readonly string SettingsTempPath = SettingsPath + ".tmp";

    public bool LightTheme { get; set; }
    public bool HighContrast { get; set; }
    public bool LargeText { get; set; }
    public bool StrongFocus { get; set; } = true;
    public bool ReducedMotion { get; set; }
    public bool CheckUpdatesOnStartup { get; set; } = true;
    public bool KeepLauncherOpen { get; set; } = true;
    public bool AutoMemory { get; set; } = true;
    public int MinimumRamMb { get; set; } = 1024;
    public int MaximumRamMb { get; set; } = 4096;

    public static LauncherPreferences Load()
    {
        foreach (var path in new[] { SettingsPath, SettingsBackupPath, SettingsTempPath })
        {
            var loaded = TryLoad(path);
            if (loaded is null) continue;

            loaded.NormalizeMemory();
            return loaded;
        }

        var defaults = new LauncherPreferences();
        defaults.NormalizeMemory();
        return defaults;
    }

    private static LauncherPreferences? TryLoad(string path)
    {
        try
        {
            if (!File.Exists(path)) return null;
            return JsonSerializer.Deserialize<LauncherPreferences>(File.ReadAllText(path));
        }
        catch
        {
            return null;
        }
    }

    public void NormalizeMemory()
    {
        if (AutoMemory)
        {
            MaximumRamMb = SystemMemoryInfo.GetRecommendedMinecraftMaximumMb();
            MinimumRamMb = Math.Min(1024, MaximumRamMb);
            return;
        }

        MinimumRamMb = Math.Clamp(MinimumRamMb, 512, 32768);
        MaximumRamMb = Math.Clamp(MaximumRamMb, 1024, 32768);
        if (MinimumRamMb > MaximumRamMb) MinimumRamMb = MaximumRamMb;
    }

    public void Save()
    {
        NormalizeMemory();
        Directory.CreateDirectory(DataRoot);

        var json = JsonSerializer.Serialize(this, new JsonSerializerOptions { WriteIndented = true });
        try
        {
            File.WriteAllText(SettingsTempPath, json);

            // Preserve only a known-good previous settings file. If the primary is
            // already malformed, keep the older backup instead of replacing it with
            // corrupted data.
            if (TryLoad(SettingsPath) is not null)
            {
                File.Copy(SettingsPath, SettingsBackupPath, overwrite: true);
            }

            // Temp and primary live in the same directory, so the final replacement
            // stays on one volume and avoids exposing a partially written JSON file.
            File.Move(SettingsTempPath, SettingsPath, overwrite: true);
        }
        catch
        {
            try
            {
                if (File.Exists(SettingsTempPath)) File.Delete(SettingsTempPath);
            }
            catch
            {
                // Preference persistence must never hide the original save failure.
            }

            throw;
        }
    }
}
