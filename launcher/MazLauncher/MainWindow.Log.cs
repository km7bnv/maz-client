using System.ComponentModel;
using System.IO;
using System.Text;
using System.Windows;
using System.Windows.Controls;

namespace MazLauncher;

public partial class MainWindow
{
    private readonly StringBuilder launcherLogBuffer = new();
    private TextBox? launcherLogTextBox;
    private bool launcherLogInstalled;
    private bool startupWatchdogStarted;
    private string? lastLoggedStatus;
    private static readonly string LauncherLogDirectory = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "MazLauncher",
        "logs");
    private static readonly string LauncherLogPath = Path.Combine(LauncherLogDirectory, "launcher.log");

    protected override void OnContentRendered(EventArgs e)
    {
        base.OnContentRendered(e);
        if (launcherLogInstalled) return;
        launcherLogInstalled = true;

        InstallLauncherLogTab();
        HookLauncherActivityLogging();
        AddLauncherLog($"MazLauncher {CloudUpdateService.CurrentLauncherVersion} started");
        AddLauncherLog("Live activity logging enabled");
        StartOfflineStartupWatchdog();

        if (!string.IsNullOrWhiteSpace(StatusText.Text))
            AddLauncherLog(StatusText.Text);
    }

    private void StartOfflineStartupWatchdog()
    {
        if (startupWatchdogStarted) return;
        startupWatchdogStarted = true;

        _ = Task.Run(async () =>
        {
            await Task.Delay(TimeSpan.FromSeconds(8));
            await Dispatcher.InvokeAsync(() =>
            {
                // Startup intentionally disables controls while network/auth/catalog work runs.
                // A dead connection must never leave MazLauncher permanently unusable.
                var startupStillBlocking = !SettingsMenuButton.IsEnabled || !CheckUpdatesButton.IsEnabled;
                if (!startupStillBlocking) return;

                AddLauncherLog("Startup network work exceeded 8 seconds; releasing UI into cached/offline mode");
                SetBusy(false);
                Progress.Value = 0;

                if (session != null)
                {
                    SetLaunchButtons(true);
                    StatusText.Text = "Ready — Offline / cached mode";
                    AddLauncherLog("Cached session available; launch controls restored");
                }
                else
                {
                    SignInButton.IsEnabled = true;
                    SettingsMenuButton.IsEnabled = true;
                    CheckUpdatesButton.IsEnabled = true;
                    ThemeToggleButton.IsEnabled = true;
                    StatusText.Text = "Ready — Offline (cached account still restoring)";
                    AddLauncherLog("UI restored while cached account/session work continues");
                }
            });
        });
    }

    private void InstallLauncherLogTab()
    {
        var tabs = FindDescendant<TabControl>(this);
        if (tabs == null) return;

        launcherLogTextBox = new TextBox
        {
            IsReadOnly = true,
            TextWrapping = TextWrapping.NoWrap,
            VerticalScrollBarVisibility = ScrollBarVisibility.Auto,
            HorizontalScrollBarVisibility = ScrollBarVisibility.Auto,
            FontFamily = new System.Windows.Media.FontFamily("Consolas"),
            FontSize = 12,
            Padding = new Thickness(12),
            BorderThickness = new Thickness(1),
            Text = launcherLogBuffer.ToString()
        };

        var grid = new Grid
        {
            Background = (System.Windows.Media.Brush)FindResource("Surface"),
            Margin = new Thickness(18, 12, 18, 18)
        };
        grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        grid.RowDefinitions.Add(new RowDefinition { Height = new GridLength(1, GridUnitType.Star) });

        var title = new TextBlock
        {
            Text = "MazLauncher Activity Log",
            Foreground = (System.Windows.Media.Brush)FindResource("Text"),
            FontSize = 20,
            FontWeight = FontWeights.Bold
        };
        grid.Children.Add(title);
        Grid.SetRow(launcherLogTextBox, 1);
        launcherLogTextBox.Margin = new Thickness(0, 10, 0, 0);
        grid.Children.Add(launcherLogTextBox);

        tabs.Items.Add(new TabItem
        {
            Header = "LOG",
            Content = grid
        });
    }

    private void HookLauncherActivityLogging()
    {
        var statusDescriptor = DependencyPropertyDescriptor.FromProperty(TextBlock.TextProperty, typeof(TextBlock));
        statusDescriptor?.AddValueChanged(StatusText, (_, _) =>
        {
            var text = StatusText.Text?.Trim();
            if (string.IsNullOrWhiteSpace(text) || string.Equals(text, lastLoggedStatus, StringComparison.Ordinal)) return;
            lastLoggedStatus = text;
            AddLauncherLog(text);
        });

        AddHandler(Button.ClickEvent, new RoutedEventHandler((sender, args) =>
        {
            if (args.OriginalSource is not DependencyObject source) return;
            var button = FindAncestor<Button>(source);
            if (button == null) return;
            var label = button.Content switch
            {
                string s when !string.IsNullOrWhiteSpace(s) => s.Trim(),
                _ => !string.IsNullOrWhiteSpace(button.Name) ? button.Name : "button"
            };
            AddLauncherLog($"UI action: {label}");
        }), true);

        Closing += (_, _) => AddLauncherLog("MazLauncher closing");
        Application.Current.DispatcherUnhandledException += (_, ex) =>
            AddLauncherLog($"ERROR: {ex.Exception.GetType().Name}: {ex.Exception.Message}");
    }

    private void AddLauncherLog(string message)
    {
        if (string.IsNullOrWhiteSpace(message)) return;
        var line = $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss.fff}] {message.Trim()}";
        launcherLogBuffer.AppendLine(line);

        try
        {
            Directory.CreateDirectory(LauncherLogDirectory);
            File.AppendAllText(LauncherLogPath, line + Environment.NewLine);
        }
        catch
        {
            // Logging must never break the launcher.
        }

        if (launcherLogTextBox != null)
        {
            launcherLogTextBox.AppendText(line + Environment.NewLine);
            launcherLogTextBox.ScrollToEnd();
        }
    }

    private static T? FindDescendant<T>(DependencyObject root) where T : DependencyObject
    {
        for (var i = 0; i < System.Windows.Media.VisualTreeHelper.GetChildrenCount(root); i++)
        {
            var child = System.Windows.Media.VisualTreeHelper.GetChild(root, i);
            if (child is T match) return match;
            var nested = FindDescendant<T>(child);
            if (nested != null) return nested;
        }
        return null;
    }

    private static T? FindAncestor<T>(DependencyObject start) where T : DependencyObject
    {
        DependencyObject? current = start;
        while (current != null)
        {
            if (current is T match) return match;
            current = System.Windows.Media.VisualTreeHelper.GetParent(current);
        }
        return null;
    }
}
