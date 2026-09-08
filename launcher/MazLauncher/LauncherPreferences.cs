using System.IO;
using System.Text.Json;

namespace MazLauncher;

public sealed class LauncherPreferences
{
    private static readonly string DataRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher");
    private static readonly string SettingsPath = Path.Combine(DataRoot, "settings.json");

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
        try
        {
            if (File.Exists(SettingsPath))
            {
                var loaded = JsonSerializer.Deserialize<LauncherPreferences>(File.ReadAllText(SettingsPath)) ?? new LauncherPreferences();
                loaded.NormalizeMemory();
                return loaded;
            }
        }
        catch { }

        var defaults = new LauncherPreferences();
        defaults.NormalizeMemory();
        return defaults;
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
        File.WriteAllText(SettingsPath, JsonSerializer.Serialize(this, new JsonSerializerOptions { WriteIndented = true }));
    }
}
