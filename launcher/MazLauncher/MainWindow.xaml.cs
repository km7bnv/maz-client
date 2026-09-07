using System.Windows;
using CmlLib.Core.Auth;

namespace MazLauncher;

public partial class MainWindow : Window
{
    private readonly LauncherService launcher = new();
    private MSession? session;

    public MainWindow()
    {
        InitializeComponent();
        SetLaunchButtons(false);
        Loaded += MainWindow_Loaded;
    }

    private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
    {
        try
        {
            SetBusy(true, "Restoring cached session...");
            session = await launcher.TryRestoreSessionAsync();

            if (session != null)
            {
                AccountText.Text = session.Username;
                SignInButton.Content = "Signed in";
                SetLaunchButtons(true);
                StatusText.Text = "Ready — cached account restored";
            }
            else
            {
                StatusText.Text = "Ready";
            }
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
            SignInButton.Content = "Signed in";
            SetLaunchButtons(true);
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

    private async void VanillaButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null) return;
        await RunLaunchAsync(() => launcher.LaunchVanillaAsync(session, UpdateProgress), "Vanilla");
    }

    private async void MazButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null) return;
        await RunLaunchAsync(() => launcher.LaunchMazAsync(session, UpdateProgress), "MazClient");
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
        VanillaButton.IsEnabled = !busy && session != null;
        MazButton.IsEnabled = !busy && session != null;
        if (status != null) StatusText.Text = status;
    }

    private void SetLaunchButtons(bool enabled)
    {
        VanillaButton.IsEnabled = enabled;
        MazButton.IsEnabled = enabled;
    }
}
