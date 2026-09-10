using System.Windows.Controls;

namespace MazLauncher;

public partial class MainWindow
{
    private readonly BetterBlockEntitiesService betterBlockEntities = new();
    private readonly BadOptimizationsService badOptimizations = new();
    private readonly DynamicFpsService dynamicFps = new();
    private readonly FerriteCoreService ferriteCore = new();
    private readonly KryptonService krypton = new();
    private readonly ImmediatelyFastService immediatelyFast = new();
    private readonly DebugifyService debugify = new();
    private readonly ModernFixService modernFix = new();
    private bool performanceStackInitialized;

    private async Task InitializeManagedPerformanceStackAsync()
    {
        if (performanceStackInitialized) return;
        performanceStackInitialized = true;

        MazClientVersionBox.SelectionChanged += MazClientPerformanceVersionChanged;
        await EnsureManagedPerformanceStackForSelectedVersionAsync();
    }

    private async void MazClientPerformanceVersionChanged(object sender, SelectionChangedEventArgs e)
    {
        await EnsureManagedPerformanceStackForSelectedVersionAsync();
    }

    private async Task EnsureManagedPerformanceStackForSelectedVersionAsync()
    {
        if (MazClientVersionBox.SelectedItem is not string version || string.IsNullOrWhiteSpace(version)) return;

        var failures = new List<string>();

        try
        {
            UpdateProgress("Checking Better Block Entities optimization...", 18);
            await betterBlockEntities.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("Better Block Entities");
            AddLauncherLog("Better Block Entities preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking BadOptimizations...", 26);
            await badOptimizations.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("BadOptimizations");
            AddLauncherLog("BadOptimizations preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking Dynamic FPS...", 34);
            await dynamicFps.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("Dynamic FPS");
            AddLauncherLog("Dynamic FPS preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking FerriteCore memory optimization...", 42);
            await ferriteCore.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("FerriteCore");
            AddLauncherLog("FerriteCore preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking Krypton network optimization...", 50);
            await krypton.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("Krypton");
            AddLauncherLog("Krypton preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking ImmediatelyFast rendering optimization...", 58);
            await immediatelyFast.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("ImmediatelyFast");
            AddLauncherLog("ImmediatelyFast preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking Debugify vanilla bug fixes...", 66);
            await debugify.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("Debugify");
            AddLauncherLog("Debugify preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking ModernFix performance and memory fixes...", 74);
            await modernFix.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("ModernFix-mVUS");
            AddLauncherLog("ModernFix-mVUS preparation deferred: " + ex.Message);
        }

        RefreshMods();
        Progress.Value = 0;
        if (session != null)
        {
            StatusText.Text = failures.Count == 0
                ? "Ready — managed performance and bug-fix stack verified"
                : "Ready — some optional managed mods could not be refreshed";
        }
    }
}
