using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using CmlLib.Core.Auth;
using Microsoft.Win32;

namespace MazLauncher;

public partial class MainWindow : Window
{
    private readonly LauncherService launcher = new();
    private readonly CloudUpdateService cloudUpdates = new();
    private readonly LauncherPreferences preferences = LauncherPreferences.Load();
    private readonly HttpClient uiHttp = new();
    private MSession? session;
    private bool applyingPreferences;

    public MainWindow()
    {
        InitializeComponent();
        uiHttp.DefaultRequestHeaders.UserAgent.ParseAdd($"MazLauncher/{CloudUpdateService.CurrentLauncherVersion}");
        LoadPreferencesIntoUi();
        ApplyPreferences();
        SetLaunchButtons(false);
        UpdateAccountControls();
        Loaded += MainWindow_Loaded;
    }

    private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
    {
        var changelogTask = LoadChangelogAsync();
        SetBusy(true, "Loading installations...");
        try { await LoadVersionListsAsync(); }
        catch (Exception ex) { StatusText.Text = "Version catalog partially unavailable"; Debug.WriteLine(ex); }

        if (preferences.CheckUpdatesOnStartup)
        {
            try { await launcher.CheckForLauncherUpdateAsync(); }
            catch (Exception ex) { Debug.WriteLine(ex); }
        }

        try
        {
            StatusText.Text = "Restoring cached session...";
            session = await launcher.TryRestoreSessionAsync();
            if (session != null)
            {
                AccountText.Text = session.Username;
                SetLaunchButtons(true);
                StatusText.Text = "Cached account restored — preparing offline cache...";
                await CacheLatestOfflineAsync();
            }
            else StatusText.Text = "Ready";
            UpdateAccountControls();
        }
        catch (Exception ex) { StatusText.Text = "Ready"; Debug.WriteLine(ex); }
        finally { Progress.Value = 0; SetBusy(false); }

        await changelogTask;
    }

    private void SettingsMenuButton_Click(object sender, RoutedEventArgs e) => SettingsOverlay.Visibility = Visibility.Visible;
    private void CloseSettingsButton_Click(object sender, RoutedEventArgs e) => SettingsOverlay.Visibility = Visibility.Collapsed;

    private void LoadPreferencesIntoUi()
    {
        applyingPreferences = true;
        KeepLauncherOpenCheck.IsChecked = preferences.KeepLauncherOpen;
        CheckUpdatesOnStartupCheck.IsChecked = preferences.CheckUpdatesOnStartup;
        HighContrastCheck.IsChecked = preferences.HighContrast;
        LargeTextCheck.IsChecked = preferences.LargeText;
        StrongFocusCheck.IsChecked = preferences.StrongFocus;
        ReducedMotionCheck.IsChecked = preferences.ReducedMotion;
        applyingPreferences = false;
    }

    private void SettingsChanged(object sender, RoutedEventArgs e)
    {
        if (applyingPreferences) return;
        preferences.KeepLauncherOpen = KeepLauncherOpenCheck.IsChecked == true;
        preferences.CheckUpdatesOnStartup = CheckUpdatesOnStartupCheck.IsChecked == true;
        preferences.HighContrast = HighContrastCheck.IsChecked == true;
        preferences.LargeText = LargeTextCheck.IsChecked == true;
        preferences.StrongFocus = StrongFocusCheck.IsChecked == true;
        preferences.ReducedMotion = ReducedMotionCheck.IsChecked == true;
        preferences.Save();
        ApplyPreferences();
    }

    private void ThemeToggleButton_Click(object sender, RoutedEventArgs e)
    {
        preferences.LightTheme = !preferences.LightTheme;
        preferences.Save();
        ApplyPreferences();
    }

    private void ApplyPreferences()
    {
        if (preferences.HighContrast)
        {
            SetBrush("Bg", Colors.Black);
            SetBrush("Surface", Colors.Black);
            SetBrush("Surface2", Color.FromRgb(15, 15, 15));
            SetBrush("SurfaceHover", Color.FromRgb(35, 35, 35));
            SetBrush("InputBg", Colors.Black);
            SetBrush("InputBorder", Colors.White);
            SetBrush("Border", Colors.White);
            SetBrush("Text", Colors.White);
            SetBrush("Muted", Colors.White);
            SetBrush("Placeholder", Color.FromRgb(220, 220, 220));
            SetBrush("Accent", Colors.Yellow);
            SetBrush("AccentHover", Color.FromRgb(255, 238, 0));
            SetBrush("AccentText", Colors.Black);
        }
        else if (preferences.LightTheme)
        {
            SetBrush("Bg", Color.FromRgb(241, 245, 249));
            SetBrush("Surface", Colors.White);
            SetBrush("Surface2", Color.FromRgb(226, 232, 240));
            SetBrush("SurfaceHover", Color.FromRgb(203, 213, 225));
            SetBrush("InputBg", Color.FromRgb(248, 250, 252));
            SetBrush("InputBorder", Color.FromRgb(148, 163, 184));
            SetBrush("Border", Color.FromRgb(203, 213, 225));
            SetBrush("Text", Color.FromRgb(15, 23, 42));
            SetBrush("Muted", Color.FromRgb(71, 85, 105));
            SetBrush("Placeholder", Color.FromRgb(100, 116, 139));
            SetBrush("Accent", Color.FromRgb(88, 101, 242));
            SetBrush("AccentHover", Color.FromRgb(71, 82, 196));
            SetBrush("AccentText", Colors.White);
        }
        else
        {
            SetBrush("Bg", Color.FromRgb(15, 23, 42));
            SetBrush("Surface", Color.FromRgb(30, 41, 59));
            SetBrush("Surface2", Color.FromRgb(38, 52, 73));
            SetBrush("SurfaceHover", Color.FromRgb(51, 65, 85));
            SetBrush("InputBg", Color.FromRgb(23, 32, 51));
            SetBrush("InputBorder", Color.FromRgb(71, 85, 105));
            SetBrush("Border", Color.FromRgb(51, 65, 85));
            SetBrush("Text", Color.FromRgb(248, 250, 252));
            SetBrush("Muted", Color.FromRgb(203, 213, 225));
            SetBrush("Placeholder", Color.FromRgb(148, 163, 184));
            SetBrush("Accent", Color.FromRgb(99, 102, 241));
            SetBrush("AccentHover", Color.FromRgb(129, 140, 248));
            SetBrush("AccentText", Colors.White);
        }

        Background = (Brush)Resources["Bg"];
        RootGrid.LayoutTransform = preferences.LargeText ? new ScaleTransform(1.06, 1.06) : Transform.Identity;
        SettingsMenuButton.BorderThickness = preferences.StrongFocus ? new Thickness(2) : new Thickness(1);
        ThemeToggleButton.Content = preferences.LightTheme ? "☾ DARK" : "☀ LIGHT";
        ThemeToggleButton.ToolTip = preferences.LightTheme ? "Switch to Dark launcher theme" : "Switch to Light launcher theme";
    }

    private void SetBrush(string key, Color color) => Resources[key] = new SolidColorBrush(color);

    private async Task LoadChangelogAsync()
    {
        try
        {
            ChangelogText.Text = await uiHttp.GetStringAsync("https://raw.githubusercontent.com/km7bnv/maz-client/main/CHANGELOG.md");
        }
        catch (Exception ex)
        {
            Debug.WriteLine(ex);
            ChangelogText.Text = "Could not load the changelog right now. Check your internet connection and try again later.";
        }
    }

    private async Task LoadVersionListsAsync()
    {
        var vanillaTask = launcher.GetVanillaVersionsAsync();
        var mazTask = launcher.GetMazClientVersionsAsync();
        await Task.WhenAll(vanillaTask, mazTask);
        var vanilla = await vanillaTask;
        VanillaVersionBox.ItemsSource = vanilla;
        VanillaVersionBox.SelectedItem = vanilla.FirstOrDefault(v => string.Equals(v, LauncherService.MinecraftVersion, StringComparison.OrdinalIgnoreCase)) ?? vanilla.FirstOrDefault();
        var maz = await mazTask;
        MazClientVersionBox.ItemsSource = maz;
        ModsMazVersionBox.ItemsSource = maz;
        MazClientVersionBox.SelectedItem = maz.FirstOrDefault();
        ModsMazVersionBox.SelectedItem = maz.FirstOrDefault();
        UpdateSelectedVersionLabels();
        RefreshMods();
    }

    private async Task CacheLatestOfflineAsync()
    {
        if (session == null) return;
        try
        {
            await launcher.WarmLatestOfflineCacheAsync(session, UpdateProgress);
            StatusText.Text = "Ready — latest Vanilla + MazClient cached for offline play";
        }
        catch (Exception ex)
        {
            Debug.WriteLine(ex);
            StatusText.Text = "Ready — offline cache will finish next time internet is available";
        }
    }

    private async void SignInButton_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            SetBusy(true, "Opening Microsoft sign-in...");
            session = await launcher.SignInAsync();
            AccountText.Text = session.Username;
            SetLaunchButtons(true);
            UpdateAccountControls();
            StatusText.Text = "Account cached — preparing latest versions for offline play...";
            await CacheLatestOfflineAsync();
            Progress.Value = 0;
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "Microsoft sign-in failed", MessageBoxButton.OK, MessageBoxImage.Error); StatusText.Text = "Sign-in failed"; }
        finally { SetBusy(false); }
    }

    private async void SignOutButton_Click(object sender, RoutedEventArgs e)
    {
        if (MessageBox.Show("Are you sure you want to sign out of MazLauncher? Your cached Microsoft account will be removed from this launcher.", "Sign out of MazLauncher?", MessageBoxButton.YesNo, MessageBoxImage.Question, MessageBoxResult.No) != MessageBoxResult.Yes) return;
        try
        {
            SetBusy(true, "Signing out...");
            await launcher.SignOutAsync();
            session = null;
            AccountText.Text = "Not signed in";
            SetLaunchButtons(false);
            UpdateAccountControls();
            StatusText.Text = "Signed out — cached account removed";
            Progress.Value = 0;
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "Could not sign out", MessageBoxButton.OK, MessageBoxImage.Error); StatusText.Text = "Sign-out failed"; }
        finally { SetBusy(false); }
    }

    private async void CheckUpdatesButton_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            SetBusy(true, "Checking for updates...");
            var manifest = await launcher.GetCloudManifestAsync();
            if (manifest == null) { StatusText.Text = "Could not reach the update server"; return; }
            if (await cloudUpdates.HasLauncherUpdateAsync())
            {
                StatusText.Text = $"MazLauncher {manifest.LauncherVersion} found — applying update...";
                await cloudUpdates.DownloadAndApplyLauncherUpdateAsync();
                return;
            }
            StatusText.Text = $"Up to date — MazLauncher {CloudUpdateService.CurrentLauncherVersion}, MazClient cloud {manifest.MazClientVersion}";
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "Update check failed", MessageBoxButton.OK, MessageBoxImage.Error); StatusText.Text = "Update check failed"; }
        finally { SetBusy(false); }
    }

    private async void VanillaButton_Click(object sender, RoutedEventArgs e) => await LaunchSelectedVanillaAsync();
    private async void MazButton_Click(object sender, RoutedEventArgs e) => await LaunchSelectedMazAsync();
    private async void LaunchVanillaSelectedButton_Click(object sender, RoutedEventArgs e) => await LaunchSelectedVanillaAsync();
    private async void LaunchMazSelectedButton_Click(object sender, RoutedEventArgs e) => await LaunchSelectedMazAsync();

    private async Task LaunchSelectedVanillaAsync()
    {
        if (session == null) return;
        var version = VanillaVersionBox.SelectedItem as string;
        if (string.IsNullOrWhiteSpace(version)) { MessageBox.Show("Choose a Vanilla version first."); return; }
        await RunLaunchAsync(() => launcher.LaunchVanillaAsync(session, version, UpdateProgress), $"Vanilla {version}");
    }

    private async Task LaunchSelectedMazAsync()
    {
        if (session == null) return;
        var version = MazClientVersionBox.SelectedItem as string;
        if (string.IsNullOrWhiteSpace(version)) { MessageBox.Show("Choose a MazClient version first."); return; }
        await RunLaunchAsync(() => launcher.LaunchMazAsync(session, version, UpdateProgress), $"MazClient {version}");
        RefreshMods();
    }

    private void VanillaVersionBox_SelectionChanged(object sender, SelectionChangedEventArgs e) => UpdateSelectedVersionLabels();
    private void MazClientVersionBox_SelectionChanged(object sender, SelectionChangedEventArgs e) => UpdateSelectedVersionLabels();
    private void ModsMazVersionBox_SelectionChanged(object sender, SelectionChangedEventArgs e) => RefreshMods();

    private void UpdateSelectedVersionLabels()
    {
        if (VanillaSelectedText != null) VanillaSelectedText.Text = VanillaVersionBox?.SelectedItem is string vanilla ? $"Minecraft {vanilla}" : "Choose a version";
        if (MazVersionText != null) MazVersionText.Text = MazClientVersionBox?.SelectedItem is string maz ? $"MazClient {maz}" : "Choose a version";
    }

    private string? SelectedModsVersion() => ModsMazVersionBox.SelectedItem as string;
    private void RefreshMods()
    {
        if (ModList == null || ModsMazVersionBox == null) return;
        var version = SelectedModsVersion();
        ModList.ItemsSource = string.IsNullOrWhiteSpace(version) ? Array.Empty<string>() : launcher.GetInstalledMods(version);
    }

    private void AddModButton_Click(object sender, RoutedEventArgs e)
    {
        var version = SelectedModsVersion(); if (string.IsNullOrWhiteSpace(version)) return;
        var picker = new OpenFileDialog { Filter = "Fabric mod JAR (*.jar)|*.jar", Multiselect = false, Title = $"Add mod to MazClient {version}" };
        if (picker.ShowDialog() != true) return;
        try { launcher.AddMod(version, picker.FileName); RefreshMods(); StatusText.Text = $"Added {Path.GetFileName(picker.FileName)} to MazClient {version}"; }
        catch (Exception ex) { MessageBox.Show(ex.Message, "Could not add mod", MessageBoxButton.OK, MessageBoxImage.Error); }
    }
    private void ToggleModButton_Click(object sender, RoutedEventArgs e) { var version = SelectedModsVersion(); if (string.IsNullOrWhiteSpace(version) || ModList.SelectedItem is not string file) return; try { launcher.ToggleMod(version, file); RefreshMods(); } catch (Exception ex) { MessageBox.Show(ex.Message, "Could not change mod", MessageBoxButton.OK, MessageBoxImage.Warning); } }
    private void RemoveModButton_Click(object sender, RoutedEventArgs e) { var version = SelectedModsVersion(); if (string.IsNullOrWhiteSpace(version) || ModList.SelectedItem is not string file) return; if (MessageBox.Show($"Remove {file} from MazClient {version}?", "Remove mod", MessageBoxButton.YesNo, MessageBoxImage.Question) != MessageBoxResult.Yes) return; try { launcher.RemoveMod(version, file); RefreshMods(); } catch (Exception ex) { MessageBox.Show(ex.Message, "Could not remove mod", MessageBoxButton.OK, MessageBoxImage.Warning); } }
    private void OpenModsFolderButton_Click(object sender, RoutedEventArgs e) { var version = SelectedModsVersion(); if (!string.IsNullOrWhiteSpace(version)) Process.Start(new ProcessStartInfo(launcher.GetMazModsDirectory(version)) { UseShellExecute = true }); }
    private void OpenSkinManagerButton_Click(object sender, RoutedEventArgs e) => Process.Start(new ProcessStartInfo("https://www.minecraft.net/msaprofile/mygames/editskin") { UseShellExecute = true });

    private async Task RunLaunchAsync(Func<Task> launch, string mode)
    {
        try
        {
            SetBusy(true, $"Preparing {mode}...");
            await launch();
            StatusText.Text = $"{mode} launched";
            if (!preferences.KeepLauncherOpen) Close();
        }
        catch (Exception ex)
        {
            Debug.WriteLine(ex);
            MessageBox.Show(ex.Message, $"Could not launch {mode}", MessageBoxButton.OK, MessageBoxImage.Error);
            StatusText.Text = $"{mode} launch failed";
        }
        finally { SetBusy(false); }
    }

    private void UpdateProgress(string text, int percent) => Dispatcher.Invoke(() => { StatusText.Text = text; Progress.Value = Math.Clamp(percent, 0, 100); });
    private void SetBusy(bool busy, string? status = null)
    {
        SignInButton.IsEnabled = !busy; SignOutButton.IsEnabled = !busy; CheckUpdatesButton.IsEnabled = !busy; SettingsMenuButton.IsEnabled = !busy;
        ThemeToggleButton.IsEnabled = true;
        VanillaButton.IsEnabled = !busy && session != null; MazButton.IsEnabled = !busy && session != null; LaunchVanillaSelectedButton.IsEnabled = !busy && session != null; LaunchMazSelectedButton.IsEnabled = !busy && session != null;
        if (status != null) StatusText.Text = status;
    }
    private void SetLaunchButtons(bool enabled) { VanillaButton.IsEnabled = enabled; MazButton.IsEnabled = enabled; LaunchVanillaSelectedButton.IsEnabled = enabled; LaunchMazSelectedButton.IsEnabled = enabled; }
    private void UpdateAccountControls() { var signedIn = session != null; SignInButton.Visibility = signedIn ? Visibility.Collapsed : Visibility.Visible; SignOutButton.Visibility = signedIn ? Visibility.Visible : Visibility.Collapsed; }
}
