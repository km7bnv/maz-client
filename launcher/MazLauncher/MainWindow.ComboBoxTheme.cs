using System.Windows;
using System.Windows.Controls;
using System.Windows.Markup;

namespace MazLauncher;

public partial class MainWindow
{
    private void InstallComboBoxTheme()
    {
        var itemStyle = new Style(typeof(ComboBoxItem));
        itemStyle.Setters.Add(new Setter(Control.ForegroundProperty, new DynamicResourceExtension("Text")));
        itemStyle.Setters.Add(new Setter(Control.BackgroundProperty, new DynamicResourceExtension("InputBg")));
        itemStyle.Setters.Add(new Setter(Control.PaddingProperty, new Thickness(10, 7)));
        itemStyle.Setters.Add(new Setter(Control.HorizontalContentAlignmentProperty, HorizontalAlignment.Stretch));

        var hover = new Trigger { Property = ComboBoxItem.IsMouseOverProperty, Value = true };
        hover.Setters.Add(new Setter(Control.BackgroundProperty, new DynamicResourceExtension("SurfaceHover")));
        hover.Setters.Add(new Setter(Control.ForegroundProperty, new DynamicResourceExtension("Text")));
        itemStyle.Triggers.Add(hover);

        var selected = new Trigger { Property = ComboBoxItem.IsSelectedProperty, Value = true };
        selected.Setters.Add(new Setter(Control.BackgroundProperty, new DynamicResourceExtension("Accent")));
        selected.Setters.Add(new Setter(Control.ForegroundProperty, new DynamicResourceExtension("AccentText")));
        itemStyle.Triggers.Add(selected);

        foreach (var combo in FindVisualDescendants<ComboBox>(RootGrid))
        {
            combo.SetResourceReference(Control.BackgroundProperty, "InputBg");
            combo.SetResourceReference(Control.ForegroundProperty, "Text");
            combo.SetResourceReference(Control.BorderBrushProperty, "InputBorder");
            combo.ItemContainerStyle = itemStyle;
        }
    }

    private static IEnumerable<T> FindVisualDescendants<T>(DependencyObject root) where T : DependencyObject
    {
        var count = System.Windows.Media.VisualTreeHelper.GetChildrenCount(root);
        for (var i = 0; i < count; i++)
        {
            var child = System.Windows.Media.VisualTreeHelper.GetChild(root, i);
            if (child is T match) yield return match;
            foreach (var nested in FindVisualDescendants<T>(child)) yield return nested;
        }
    }
}
