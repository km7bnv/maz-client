using System.Runtime.InteropServices;
using System.Windows;

namespace MazLauncher;

public partial class App : Application
{
    [DllImport("shell32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
    private static extern int SetCurrentProcessExplicitAppUserModelID(string appID);

    protected override void OnStartup(StartupEventArgs e)
    {
        SetCurrentProcessExplicitAppUserModelID("MazClient.MazLauncher");
        LauncherDiagnostics.WriteLatest();
        SharedMinecraftSettingsService.SynchronizeAllManagedInstallations();
        base.OnStartup(e);
    }
}
