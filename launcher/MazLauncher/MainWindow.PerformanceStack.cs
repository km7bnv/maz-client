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
    private readonly SodiumReliefService sodiumRelief = new();
    private readonly GnetumService gnetum = new();
    private readonly ReesesSodiumOptionsService reesesSodiumOptions = new();
    private readonly LanguageReloadService languageReload = new();
    private readonly ModMenuService modMenu = new();
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

        // The compatibility launcher path installs its own exact 1.21.11-compatible
        // core stack during LaunchMazAsync. Do not let the 26.2-only optional services
        // race that setup and drop wrong-version JARs into the compatibility instance.
        if (string.Equals(MazMinecraftVersionBox.SelectedItem as string,
                LauncherService.CompatibilityMinecraftVersion,
                StringComparison.OrdinalIgnoreCase))
        {
            RefreshMods();
            Progress.Value = 0;
            if (session != null)
                StatusText.Text = "Ready — 1.21.11 compatibility stack will be verified at launch";
            return;
        }

        var failures = new List<string>();

        try { UpdateProgress("Checking Better Block Entities optimization...", 10); await betterBlockEntities.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Better Block Entities"); AddLauncherLog("Better Block Entities preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking BadOptimizations...", 18); await badOptimizations.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("BadOptimizations"); AddLauncherLog("BadOptimizations preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Dynamic FPS...", 26); await dynamicFps.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Dynamic FPS"); AddLauncherLog("Dynamic FPS preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking FerriteCore memory optimization...", 34); await ferriteCore.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("FerriteCore"); AddLauncherLog("FerriteCore preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Krypton network optimization...", 42); await krypton.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Krypton"); AddLauncherLog("Krypton preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking ImmediatelyFast rendering optimization...", 50); await immediatelyFast.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("ImmediatelyFast"); AddLauncherLog("ImmediatelyFast preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Debugify vanilla bug fixes...", 58); await debugify.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Debugify"); AddLauncherLog("Debugify preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking ModernFix performance and memory fixes...", 66); await modernFix.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("ModernFix-mVUS"); AddLauncherLog("ModernFix-mVUS preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Sodium Relief inventory smoothness optimization...", 74); await sodiumRelief.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Sodium Relief"); AddLauncherLog("Sodium Relief preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Gnetum HUD performance optimization...", 82); await gnetum.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Gnetum"); AddLauncherLog("Gnetum preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Reese's Sodium Options QoL...", 88); await reesesSodiumOptions.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Reese's Sodium Options"); AddLauncherLog("Reese's Sodium Options preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Language Reload QoL...", 93); await languageReload.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Language Reload"); AddLauncherLog("Language Reload preparation deferred: " + ex.Message); }
        try { UpdateProgress("Checking Mod Menu configuration hub...", 98); await modMenu.EnsureForMazClientAsync(version, AddLauncherLog); }
        catch (Exception ex) { failures.Add("Mod Menu"); AddLauncherLog("Mod Menu preparation deferred: " + ex.Message); }

        RefreshMods();
        Progress.Value = 0;
        if (session != null)
        {
            StatusText.Text = failures.Count == 0
                ? "Ready — managed performance, bug-fix, and QoL stack verified"
                : "Ready — some optional managed mods could not be refreshed";
        }
    }
}
