using System.Diagnostics;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace MazLauncher;

public partial class MainWindow
{
    private ComboBox? minimumRamBox;
    private ComboBox? maximumRamBox;
    private TextBlock? memorySummaryText;
    private TextBlock? aboutClientText;

    private static readonly int[] RamChoicesMb =
    {
        512, 1024, 1536, 2048, 3072, 4096, 6144, 8192, 12288, 16384, 24576, 32768
    };

    private void InstallSettingsExtras()
    {
        var scroll = FindSettingsDescendant<ScrollViewer>(SettingsOverlay);
        if (scroll?.Content is not StackPanel panel || panel.Children.OfType<FrameworkElement>().Any(x => Equals(x.Tag, "MazSettingsExtras")))
            return;

        foreach (var text in panel.Children.OfType<TextBlock>())
        {
            if (text.Text.StartsWith("Java, RAM and per-installation folders", StringComparison.Ordinal))
                text.Text = "Minecraft launch memory is configured below. Per-installation folders remain managed from Installations.";
        }

        var marker = new Border { Tag = "MazSettingsExtras", Height = 1, Background = Brushes.Transparent };
        panel.Children.Add(marker);

        panel.Children.Add(SectionHeader("MEMORY"));
        panel.Children.Add(new TextBlock
        {
            Text = "Choose the Java heap used by both Vanilla and MazClient launches. Settings save automatically.",
            Foreground = (Brush)Resources["Muted"],
            TextWrapping = TextWrapping.Wrap,
            Margin = new Thickness(0, 0, 0, 8)
        });

        var memoryGrid = new Grid { Margin = new Thickness(0, 0, 0, 4) };
        memoryGrid.ColumnDefinitions.Add(new ColumnDefinition());
        memoryGrid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(14) });
        memoryGrid.ColumnDefinitions.Add(new ColumnDefinition());

        minimumRamBox = CreateRamBox(preferences.MinimumRamMb);
        maximumRamBox = CreateRamBox(preferences.MaximumRamMb);
        var minPanel = CreateLabeledControl("Minimum RAM", minimumRamBox);
        var maxPanel = CreateLabeledControl("Maximum RAM", maximumRamBox);
        Grid.SetColumn(maxPanel, 2);
        memoryGrid.Children.Add(minPanel);
        memoryGrid.Children.Add(maxPanel);
        panel.Children.Add(memoryGrid);

        memorySummaryText = new TextBlock
        {
            Foreground = (Brush)Resources["Muted"],
            Margin = new Thickness(0, 5, 0, 18),
            TextWrapping = TextWrapping.Wrap
        };
        panel.Children.Add(memorySummaryText);
        UpdateMemorySummary();

        panel.Children.Add(SectionHeader("ABOUT"));
        var about = new Border
        {
            Background = (Brush)Resources["Surface"],
            CornerRadius = new CornerRadius(10),
            Padding = new Thickness(14),
            Margin = new Thickness(0, 8, 0, 8)
        };
        var aboutPanel = new StackPanel();
        aboutPanel.Children.Add(new TextBlock
        {
            Text = $"MazLauncher {CloudUpdateService.CurrentLauncherVersion}",
            Foreground = (Brush)Resources["Text"],
            FontWeight = FontWeights.SemiBold,
            FontSize = 16
        });
        aboutClientText = new TextBlock
        {
            Text = "MazClient: loading current release…",
            Foreground = (Brush)Resources["Muted"],
            Margin = new Thickness(0, 4, 0, 0)
        };
        aboutPanel.Children.Add(aboutClientText);
        aboutPanel.Children.Add(new TextBlock
        {
            Text = $"Minecraft {LauncherService.MinecraftVersion} • Fabric Loader {LauncherService.FabricLoaderVersion}",
            Foreground = (Brush)Resources["Muted"],
            Margin = new Thickness(0, 3, 0, 0)
        });
        aboutPanel.Children.Add(new TextBlock
        {
            Text = "MazClient launcher and client project",
            Foreground = (Brush)Resources["Muted"],
            Margin = new Thickness(0, 3, 0, 10)
        });
        var repoButton = new Button
        {
            Content = "OPEN GITHUB",
            Width = 126,
            HorizontalAlignment = HorizontalAlignment.Left,
            Style = (Style)Resources["MazButton"]
        };
        repoButton.Click += (_, _) => Process.Start(new ProcessStartInfo("https://github.com/km7bnv/maz-client") { UseShellExecute = true });
        aboutPanel.Children.Add(repoButton);
        about.Child = aboutPanel;
        panel.Children.Add(about);
    }

    private StackPanel CreateLabeledControl(string label, Control control)
    {
        var panel = new StackPanel();
        panel.Children.Add(new TextBlock
        {
            Text = label,
            Foreground = (Brush)Resources["Muted"],
            FontWeight = FontWeights.SemiBold,
            Margin = new Thickness(0, 0, 0, 5)
        });
        panel.Children.Add(control);
        return panel;
    }

    private ComboBox CreateRamBox(int selectedMb)
    {
        var selectedIndex = Array.FindIndex(RamChoicesMb, value => value == selectedMb);
        var box = new ComboBox
        {
            ItemsSource = RamChoicesMb.Select(FormatRam).ToArray(),
            SelectedIndex = selectedIndex >= 0 ? selectedIndex : Array.IndexOf(RamChoicesMb, 4096)
        };
        box.SelectionChanged += MemoryBox_SelectionChanged;
        return box;
    }

    private void MemoryBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (minimumRamBox == null || maximumRamBox == null) return;
        var min = RamChoicesMb[Math.Clamp(minimumRamBox.SelectedIndex, 0, RamChoicesMb.Length - 1)];
        var max = RamChoicesMb[Math.Clamp(maximumRamBox.SelectedIndex, 0, RamChoicesMb.Length - 1)];

        if (min > max)
        {
            if (ReferenceEquals(sender, minimumRamBox))
            {
                max = min;
                maximumRamBox.SelectedIndex = Array.IndexOf(RamChoicesMb, max);
            }
            else
            {
                min = max;
                minimumRamBox.SelectedIndex = Array.IndexOf(RamChoicesMb, min);
            }
        }

        preferences.MinimumRamMb = min;
        preferences.MaximumRamMb = max;
        preferences.Save();
        UpdateMemorySummary();
        AddLauncherLog($"Minecraft memory changed: min {min} MB, max {max} MB");
    }

    private void UpdateMemorySummary()
    {
        if (memorySummaryText == null) return;
        memorySummaryText.Text = $"Java memory: {FormatRam(preferences.MinimumRamMb)} minimum • {FormatRam(preferences.MaximumRamMb)} maximum. Changes apply on the next launch.";
    }

    private void UpdateAboutClientVersion()
    {
        if (aboutClientText == null) return;
        aboutClientText.Text = MazClientVersionBox?.SelectedItem is string version
            ? $"MazClient {version}"
            : "MazClient: no release selected";
    }

    private TextBlock SectionHeader(string text) => new()
    {
        Text = text,
        Foreground = (Brush)Resources["Muted"],
        FontWeight = FontWeights.Bold,
        Margin = new Thickness(0, 16, 0, 4)
    };

    private static string FormatRam(int mb) => mb >= 1024 && mb % 1024 == 0 ? $"{mb / 1024} GB" : $"{mb} MB";

    private static T? FindSettingsDescendant<T>(DependencyObject root) where T : DependencyObject
    {
        var count = VisualTreeHelper.GetChildrenCount(root);
        for (var i = 0; i < count; i++)
        {
            var child = VisualTreeHelper.GetChild(root, i);
            if (child is T match) return match;
            var nested = FindSettingsDescendant<T>(child);
            if (nested != null) return nested;
        }
        return null;
    }
}
