using System.Windows.Controls;

namespace MazLauncher;

public partial class MainWindow
{
    private readonly BetterBlockEntitiesService betterBlockEntities = new();
    private readonly BadOptimizationsService badOptimizations = new();
    private readonly DynamicFpsService dynamicFps = new();
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
            UpdateProgress("Checking Better Block Entities optimization...", 34);
            await betterBlockEntities.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("Better Block Entities");
            AddLauncherLog("Better Block Entities preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking BadOptimizations...", 42);
            await badOptimizations.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("BadOptimizations");
            AddLauncherLog("BadOptimizations preparation deferred: " + ex.Message);
        }

        try
        {
            UpdateProgress("Checking Dynamic FPS...", 50);
            await dynamicFps.EnsureForMazClientAsync(version, AddLauncherLog);
        }
        catch (Exception ex)
        {
            failures.Add("Dynamic FPS");
            AddLauncherLog("Dynamic FPS preparation deferred: " + ex.Message);
        }

        RefreshMods();
        Progress.Value = 0;
        if (session != null)
        {
            StatusText.Text = failures.Count == 0
                ? "Ready — managed performance stack verified"
                : "Ready — some optional performance mods could not be refreshed";
        }
    }
}
