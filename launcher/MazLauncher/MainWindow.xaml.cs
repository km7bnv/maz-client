using System.Windows;
using CmlLib.Core.Auth;

namespace MazLauncher;

public partial class MainWindow : Window
{
    private readonly LauncherService launcher = new();
    private readonly CloudUpdateService cloudUpdates = new();
    private MSession? session;

    public MainWindow()
    {
        InitializeComponent();
        SetLaunchButtons(false);
        UpdateAccountControls();
        Loaded += MainWindow_Loaded;
    }

    private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
    {
        SetBusy(true, "Checking cloud updates...");

        try
        {
            var manifest = await launcher.GetCloudManifestAsync();
            MazVersionText.Text = manifest == null
                ? "MazClient version unavailable"
                : $"MazClient {manifest.MazClientVersion}";
        }
        catch (Exception ex)
        {
            MazVersionText.Text = "MazClient (offline cache)";
            System.Diagnostics.Debug.WriteLine(ex);
        }

        try
        {
            await launcher.CheckForLauncherUpdateAsync();
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine(ex);
        }

        try
        {
            StatusText.Text = "Restoring cached session...";
            session = await launcher.TryRestoreSessionAsync();

            if (session != null)
            {
                AccountText.Text = session.Username;
                SetLaunchButtons(true);
                StatusText.Text = "Ready — cached account restored";
            }
            else
            {
                StatusText.Text = "Ready";
            }

            UpdateAccountControls();
        }
        catch (Exception ex)
        {
            StatusText.Text = "Ready";
            System.Diagnostics.Debug.WriteLine(ex);
        }
        finally
        {
            Progress.Value = 0;
            SetBusy(false);
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
            StatusText.Text = "Ready — account cached";
            Progress.Value = 0;
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "Microsoft sign-in failed", MessageBoxButton.OK, MessageBoxImage.Error);
            StatusText.Text = "Sign-in failed";
        }
        finally
        {
            SetBusy(false);
        }
    }

    private async void SignOutButton_Click(object sender, RoutedEventArgs e)
    {
        var answer = MessageBox.Show(
            "Are you sure you want to sign out of MazLauncher? Your cached Microsoft account will be removed from this launcher.",
            "Sign out of MazLauncher?",
            MessageBoxButton.YesNo,
            MessageBoxImage.Question,
            MessageBoxResult.No
        );

        if (answer != MessageBoxResult.Yes)
            return;

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
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "Could not sign out", MessageBoxButton.OK, MessageBoxImage.Error);
            StatusText.Text = "Sign-out failed";
        }
        finally
        {
            SetBusy(false);
        }
    }

    private async void CheckUpdatesButton_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            SetBusy(true, "Checking for updates...");
            var manifest = await launcher.GetCloudManifestAsync();
            if (manifest == null)
            {
                StatusText.Text = "Could not reach the update server";
                return;
            }

            MazVersionText.Text = $"MazClient {manifest.MazClientVersion}";

            if (await cloudUpdates.HasLauncherUpdateAsync())
            {
                StatusText.Text = $"MazLauncher {manifest.LauncherVersion} found — applying update...";
                await cloudUpdates.DownloadAndApplyLauncherUpdateAsync();
                return;
            }

            StatusText.Text =
                $"Up to date — MazLauncher {CloudUpdateService.CurrentLauncherVersion}, MazClient cloud {manifest.MazClientVersion}";
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "Update check failed", MessageBoxButton.OK, MessageBoxImage.Error);
            StatusText.Text = "Update check failed";
        }
        finally
        {
            SetBusy(false);
        }
    }

    private async void VanillaButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null) return;
        await RunLaunchAsync(() => launcher.LaunchVanillaAsync(session, UpdateProgress), "Vanilla");
    }

    private async void MazButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null) return;
        await RunLaunchAsync(() => launcher.LaunchMazAsync(session, UpdateProgress), "MazClient");

        var manifest = await launcher.GetCloudManifestAsync();
        if (manifest != null)
            MazVersionText.Text = $"MazClient {manifest.MazClientVersion}";
    }

    private async Task RunLaunchAsync(Func<Task> launch, string mode)
    {
        try
        {
            SetBusy(true, $"Preparing {mode}...");
            await launch();
            StatusText.Text = $"{mode} launched";
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.ToString(), $"Could not launch {mode}", MessageBoxButton.OK, MessageBoxImage.Error);
            StatusText.Text = $"{mode} launch failed";
        }
        finally
        {
            SetBusy(false);
        }
    }

    private void UpdateProgress(string text, int percent)
    {
        Dispatcher.Invoke(() =>
        {
            StatusText.Text = text;
            Progress.Value = Math.Clamp(percent, 0, 100);
        });
    }

    private void SetBusy(bool busy, string? status = null)
    {
        SignInButton.IsEnabled = !busy;
        SignOutButton.IsEnabled = !busy;
        CheckUpdatesButton.IsEnabled = !busy;
        VanillaButton.IsEnabled = !busy && session != null;
        MazButton.IsEnabled = !busy && session != null;
        if (status != null) StatusText.Text = status;
    }

    private void SetLaunchButtons(bool enabled)
    {
        VanillaButton.IsEnabled = enabled;
        MazButton.IsEnabled = enabled;
    }

    private void UpdateAccountControls()
    {
        var signedIn = session != null;
        SignInButton.Visibility = signedIn ? Visibility.Collapsed : Visibility.Visible;
        SignOutButton.Visibility = signedIn ? Visibility.Visible : Visibility.Collapsed;
    }
}
