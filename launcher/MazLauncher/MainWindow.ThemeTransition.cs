using System.Windows;
using System.Windows.Media.Animation;

namespace MazLauncher;

public partial class MainWindow
{
    private bool themeTransitionRunning;

    private async Task AnimateThemeTransitionAsync(Action applyTheme)
    {
        if (preferences.ReducedMotion)
        {
            applyTheme();
            return;
        }

        if (themeTransitionRunning) return;
        themeTransitionRunning = true;
        ThemeToggleButton.IsEnabled = false;

        try
        {
            var fadeOutDone = new TaskCompletionSource(TaskCreationOptions.RunContinuationsAsynchronously);
            var fadeOut = new DoubleAnimation
            {
                From = RootGrid.Opacity,
                To = 0.72,
                Duration = TimeSpan.FromMilliseconds(105),
                EasingFunction = new CubicEase { EasingMode = EasingMode.EaseIn }
            };
            fadeOut.Completed += (_, _) => fadeOutDone.TrySetResult();
            RootGrid.BeginAnimation(UIElement.OpacityProperty, fadeOut);
            await fadeOutDone.Task;

            applyTheme();

            RootGrid.BeginAnimation(UIElement.OpacityProperty, null);
            RootGrid.Opacity = 0.72;

            var fadeInDone = new TaskCompletionSource(TaskCreationOptions.RunContinuationsAsynchronously);
            var fadeIn = new DoubleAnimation
            {
                From = 0.72,
                To = 1.0,
                Duration = TimeSpan.FromMilliseconds(165),
                EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut }
            };
            fadeIn.Completed += (_, _) => fadeInDone.TrySetResult();
            RootGrid.BeginAnimation(UIElement.OpacityProperty, fadeIn);
            await fadeInDone.Task;
        }
        finally
        {
            RootGrid.BeginAnimation(UIElement.OpacityProperty, null);
            RootGrid.Opacity = 1;
            ThemeToggleButton.IsEnabled = true;
            themeTransitionRunning = false;
        }
    }
}
