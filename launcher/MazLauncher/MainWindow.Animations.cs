using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;

namespace MazLauncher;

public partial class MainWindow
{
    private static readonly Duration FastMotion = TimeSpan.FromMilliseconds(120);
    private static readonly Duration NormalMotion = TimeSpan.FromMilliseconds(180);
    private static readonly Duration EntranceMotion = TimeSpan.FromMilliseconds(240);
    private readonly ManagedResourcePackService managedResourcePacks = new();
    private bool animationsInitialized;

    protected override void OnSourceInitialized(EventArgs e)
    {
        base.OnSourceInitialized(e);
        if (preferences.ReducedMotion) return;

        RootGrid.Opacity = 0;
        RootGrid.RenderTransform = new TranslateTransform(0, 8);
    }

    protected override void OnContentRendered(EventArgs e)
    {
        base.OnContentRendered(e);
        if (animationsInitialized) return;
        animationsInitialized = true;

        InitializeLauncherLogging();
        InstallSettingsExtras();
        InstallComboBoxTheme();
        InitializeAppearanceUi();
        UpdateAboutClientVersion();
        HookButtonAnimations(RootGrid);
        HookTabAnimations(RootGrid);
        AddHandler(ButtonBase.ClickEvent, new RoutedEventHandler(LauncherButtonClicked), true);
        _ = WarmManagedResourcePacksWhenReadyAsync();
        _ = InitializeManagedPerformanceStackAsync();
        AnimateWindowIn();
    }

    private async Task WarmManagedResourcePacksWhenReadyAsync()
    {
        for (var attempt = 0; attempt < 40; attempt++)
        {
            if (MazClientVersionBox?.SelectedItem is string version && !string.IsNullOrWhiteSpace(version))
            {
                try
                {
                    await managedResourcePacks.EnsureForMazClientAsync(version, AddLauncherLog);
                }
                catch (Exception ex)
                {
                    AddLauncherLog($"Managed resource-pack warmup failed: {ex.Message}");
                }
                return;
            }
            await Task.Delay(500);
        }
        AddLauncherLog("Managed resource-pack warmup skipped: MazClient version was not ready yet");
    }

    private void AnimateWindowIn()
    {
        if (preferences.ReducedMotion)
        {
            RootGrid.BeginAnimation(OpacityProperty, null);
            RootGrid.Opacity = 1;
            RootGrid.RenderTransform = Transform.Identity;
            return;
        }

        var easing = new CubicEase { EasingMode = EasingMode.EaseOut };
        RootGrid.BeginAnimation(OpacityProperty, new DoubleAnimation(0, 1, EntranceMotion) { EasingFunction = easing });

        if (RootGrid.RenderTransform is not TranslateTransform translate)
        {
            translate = new TranslateTransform(0, 8);
            RootGrid.RenderTransform = translate;
        }
        translate.BeginAnimation(TranslateTransform.YProperty, new DoubleAnimation(8, 0, EntranceMotion) { EasingFunction = easing });
    }

    private void HookButtonAnimations(DependencyObject root)
    {
        foreach (var child in VisualChildren(root))
        {
            if (child is Button button)
            {
                button.RenderTransformOrigin = new Point(0.5, 0.5);
                button.MouseEnter += AnimatedButton_MouseEnter;
                button.MouseLeave += AnimatedButton_MouseLeave;
                button.PreviewMouseLeftButtonDown += AnimatedButton_MouseDown;
                button.PreviewMouseLeftButtonUp += AnimatedButton_MouseUp;
            }
            HookButtonAnimations(child);
        }
    }

    private void HookTabAnimations(DependencyObject root)
    {
        foreach (var child in VisualChildren(root))
        {
            if (child is TabControl tabs)
            {
                tabs.SelectionChanged += (_, args) =>
                {
                    if (!ReferenceEquals(args.Source, tabs) || preferences.ReducedMotion) return;
                    if (tabs.SelectedContent is UIElement content) AnimateTabContent(content);
                };
            }
            HookTabAnimations(child);
        }
    }

    private void AnimatedButton_MouseEnter(object sender, MouseEventArgs e)
    {
        if (preferences.ReducedMotion || sender is not Button button || !button.IsEnabled) return;
        AnimateButtonScale(button, 1.025, NormalMotion);
    }

    private void AnimatedButton_MouseLeave(object sender, MouseEventArgs e)
    {
        if (sender is not Button button) return;
        if (preferences.ReducedMotion)
        {
            ResetButtonScale(button);
            return;
        }
        AnimateButtonScale(button, 1.0, NormalMotion);
    }

    private void AnimatedButton_MouseDown(object sender, MouseButtonEventArgs e)
    {
        if (preferences.ReducedMotion || sender is not Button button || !button.IsEnabled) return;
        AnimateButtonScale(button, 0.975, FastMotion);
    }

    private void AnimatedButton_MouseUp(object sender, MouseButtonEventArgs e)
    {
        if (preferences.ReducedMotion || sender is not Button button) return;
        AnimateButtonScale(button, button.IsMouseOver ? 1.025 : 1.0, FastMotion);
    }

    private static void AnimateButtonScale(Button button, double target, Duration duration)
    {
        var scale = button.RenderTransform as ScaleTransform;
        if (scale == null)
        {
            scale = new ScaleTransform(1, 1);
            button.RenderTransform = scale;
        }

        var easing = new CubicEase { EasingMode = EasingMode.EaseOut };
        scale.BeginAnimation(ScaleTransform.ScaleXProperty, new DoubleAnimation(target, duration) { EasingFunction = easing });
        scale.BeginAnimation(ScaleTransform.ScaleYProperty, new DoubleAnimation(target, duration) { EasingFunction = easing });
    }

    private static void ResetButtonScale(Button button)
    {
        if (button.RenderTransform is not ScaleTransform scale) return;
        scale.BeginAnimation(ScaleTransform.ScaleXProperty, null);
        scale.BeginAnimation(ScaleTransform.ScaleYProperty, null);
        scale.ScaleX = 1;
        scale.ScaleY = 1;
    }

    private void AnimateTabContent(UIElement content)
    {
        var easing = new CubicEase { EasingMode = EasingMode.EaseOut };
        content.Opacity = 0.65;
        content.BeginAnimation(OpacityProperty, new DoubleAnimation(0.65, 1, NormalMotion) { EasingFunction = easing });

        var translate = new TranslateTransform(8, 0);
        content.RenderTransform = translate;
        translate.BeginAnimation(TranslateTransform.XProperty, new DoubleAnimation(8, 0, NormalMotion) { EasingFunction = easing });
    }

    private void LauncherButtonClicked(object sender, RoutedEventArgs e)
    {
        if (e.OriginalSource is not DependencyObject source) return;
        var button = FindAncestor<Button>(source);
        if (button == null) return;

        if (ReferenceEquals(button, SettingsMenuButton))
        {
            UpdateAboutClientVersion();
            if (!preferences.ReducedMotion) AnimateSettingsOpen();
            return;
        }

        if (!preferences.ReducedMotion && string.Equals(button.Content?.ToString(), "✕", StringComparison.Ordinal) && IsInside(button, SettingsOverlay))
        {
            AnimateSettingsClose();
        }
    }

    private void AnimateSettingsOpen()
    {
        SettingsOverlay.Visibility = Visibility.Visible;
        SettingsOverlay.Opacity = 0;
        var translate = new TranslateTransform(18, 0);
        SettingsOverlay.RenderTransform = translate;
        var easing = new CubicEase { EasingMode = EasingMode.EaseOut };
        SettingsOverlay.BeginAnimation(OpacityProperty, new DoubleAnimation(0, 1, NormalMotion) { EasingFunction = easing });
        translate.BeginAnimation(TranslateTransform.XProperty, new DoubleAnimation(18, 0, NormalMotion) { EasingFunction = easing });
    }

    private void AnimateSettingsClose()
    {
        SettingsOverlay.Visibility = Visibility.Visible;
        SettingsOverlay.Opacity = 1;
        var translate = SettingsOverlay.RenderTransform as TranslateTransform ?? new TranslateTransform();
        SettingsOverlay.RenderTransform = translate;
        var easing = new CubicEase { EasingMode = EasingMode.EaseIn };
        var fade = new DoubleAnimation(1, 0, FastMotion) { EasingFunction = easing };
        fade.Completed += (_, _) =>
        {
            SettingsOverlay.Visibility = Visibility.Collapsed;
            SettingsOverlay.Opacity = 1;
            translate.X = 0;
        };
        SettingsOverlay.BeginAnimation(OpacityProperty, fade);
        translate.BeginAnimation(TranslateTransform.XProperty, new DoubleAnimation(0, 14, FastMotion) { EasingFunction = easing });
    }

    private static IEnumerable<DependencyObject> VisualChildren(DependencyObject parent)
    {
        var count = VisualTreeHelper.GetChildrenCount(parent);
        for (var i = 0; i < count; i++) yield return VisualTreeHelper.GetChild(parent, i);
    }

    private static T? FindAncestor<T>(DependencyObject? source) where T : DependencyObject
    {
        for (var current = source; current != null; current = VisualTreeHelper.GetParent(current))
            if (current is T match) return match;
        return null;
    }

    private static bool IsInside(DependencyObject child, DependencyObject ancestor)
    {
        for (var current = child; current != null; current = VisualTreeHelper.GetParent(current))
            if (ReferenceEquals(current, ancestor)) return true;
        return false;
    }
}
