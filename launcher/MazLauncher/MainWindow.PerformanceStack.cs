using System.Windows;
using System.Windows.Controls;

namespace MazLauncher;

public partial class MainWindow
{
    private readonly BetterBlockEntitiesService betterBlockEntities = new();
    private bool performanceStackInitialized;

    protected override async void OnContentRendered(EventArgs e)
    {
        base.OnContentRendered(e);
        if (performanceStackInitialized) return;
        performanceStackInitialized = true;

        MazClientVersionBox.SelectionChanged += MazClientPerformanceVersionChanged;
        await EnsureBetterBlockEntitiesForSelectedVersionAsync();
    }

    private async void MazClientPerformanceVersionChanged(object sender, SelectionChangedEventArgs e)
    {
        await EnsureBetterBlockEntitiesForSelectedVersionAsync();
    }

    private async Task EnsureBetterBlockEntitiesForSelectedVersionAsync()
    {
        if (MazClientVersionBox.SelectedItem is not string version || string.IsNullOrWhiteSpace(version)) return;
        try
        {
            UpdateProgress("Checking Better Block Entities optimization...", 40);
            await betterBlockEntities.EnsureForMazClientAsync(version, AddLauncherLog);
            RefreshMods();
            Progress.Value = 0;
            if (session != null) StatusText.Text = "Ready — managed performance stack verified";
        }
        catch (Exception ex)
        {
            AddLauncherLog("Better Block Entities preparation deferred: " + ex.Message);
            Progress.Value = 0;
        }
    }
}
