using System.IO;
using System.Text.Json;

namespace MazLauncher;

public sealed class LauncherPreferences
{
    private static readonly string DataRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "MazLauncher");
    private static readonly string SettingsPath = Path.Combine(DataRoot, "settings.json");

    public bool HighContrast { get; set; }
    public bool LargeText { get; set; }
    public bool StrongFocus { get; set; } = true;
    public bool ReducedMotion { get; set; }
    public bool CheckUpdatesOnStartup { get; set; } = true;
    public bool KeepLauncherOpen { get; set; } = true;

    public static LauncherPreferences Load()
    {
        try
        {
            if (File.Exists(SettingsPath))
                return JsonSerializer.Deserialize<LauncherPreferences>(File.ReadAllText(SettingsPath)) ?? new LauncherPreferences();
        }
        catch { }
        return new LauncherPreferences();
    }

    public void Save()
    {
        Directory.CreateDirectory(DataRoot);
        File.WriteAllText(SettingsPath, JsonSerializer.Serialize(this, new JsonSerializerOptions { WriteIndented = true }));
    }
}
