using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Media.Media3D;
using Microsoft.Win32;

namespace MazLauncher;

public partial class MainWindow
{
    private string? selectedSkinPath;
    private string? selectedCapePath;
    private ImageSource? previewSkinImage;
    private ImageSource? previewCapeImage;
    private Viewport3D? appearanceViewport;
    private PerspectiveCamera? appearanceCamera;
    private readonly AxisAngleRotation3D appearanceYaw = new(new Vector3D(0, 1, 0), -18);
    private readonly AxisAngleRotation3D appearancePitch = new(new Vector3D(1, 0, 0), -7);
    private Point appearanceDragStart;
    private double appearanceDragYaw;
    private double appearanceDragPitch;
    private ComboBox? ownedCapeBox;
    private TextBlock? capeStatusText;
    private Button? loadCapesButton;
    private Button? importCapeButton;
    private Button? applyCapeButton;
    private Button? hideCapeButton;
    private OwnedCape? selectedOwnedCape;

    private sealed class OwnedCape
    {
        public string Id { get; init; } = string.Empty;
        public string Alias { get; init; } = "Cape";
        public string Url { get; init; } = string.Empty;
        public bool Active { get; set; }
        public override string ToString() => Active ? $"{Alias} (active)" : Alias;
    }

    private readonly record struct UvRect(double X, double Y, double Width, double Height);
    private readonly record struct BoxUv(UvRect Top, UvRect Bottom, UvRect Right, UvRect Front, UvRect Left, UvRect Back);

    private void InitializeAppearanceUi()
    {
        if (appearanceViewport != null) return;

        SkinPreview.Visibility = Visibility.Collapsed;
        SkinPreviewPlaceholder.Text = "IMPORT A SKIN OR LOAD ACCOUNT";

        if (SkinPreview.Parent is Grid previewHost)
        {
            appearanceCamera = new PerspectiveCamera
            {
                Position = new Point3D(0, 0.05, 4.8),
                LookDirection = new Vector3D(0, 0, -4.8),
                UpDirection = new Vector3D(0, 1, 0),
                FieldOfView = 34
            };

            appearanceViewport = new Viewport3D
            {
                Camera = appearanceCamera,
                Cursor = Cursors.Hand,
                ClipToBounds = true
            };
            appearanceViewport.MouseLeftButtonDown += AppearanceViewport_MouseLeftButtonDown;
            appearanceViewport.MouseMove += AppearanceViewport_MouseMove;
            appearanceViewport.MouseLeftButtonUp += AppearanceViewport_MouseLeftButtonUp;
            appearanceViewport.MouseWheel += AppearanceViewport_MouseWheel;
            previewHost.Children.Insert(0, appearanceViewport);

            var hint = new TextBlock
            {
                Text = "DRAG TO ROTATE  •  SCROLL TO ZOOM",
                FontSize = 10,
                FontWeight = FontWeights.SemiBold,
                HorizontalAlignment = HorizontalAlignment.Center,
                VerticalAlignment = VerticalAlignment.Bottom,
                Margin = new Thickness(0, 0, 0, 2),
                IsHitTestVisible = false
            };
            hint.SetResourceReference(TextBlock.ForegroundProperty, "Muted");
            previewHost.Children.Add(hint);
        }

        SkinModelBox.SelectionChanged += (_, _) => RenderAppearancePreview();

        if (SkinStatusText.Parent is StackPanel controls)
        {
            SkinStatusText.Margin = new Thickness(0, 10, 0, 8);

            var separator = new Border { Height = 1, Margin = new Thickness(0, 4, 0, 10) };
            separator.SetResourceReference(Border.BackgroundProperty, "Border");
            controls.Children.Add(separator);

            var title = new TextBlock { Text = "CAPES", FontWeight = FontWeights.Bold, Margin = new Thickness(0, 0, 0, 6) };
            title.SetResourceReference(TextBlock.ForegroundProperty, "Muted");
            controls.Children.Add(title);

            ownedCapeBox = new ComboBox
            {
                Width = 260,
                HorizontalAlignment = HorizontalAlignment.Left,
                IsEnabled = false,
                ToolTip = "Capes owned by the signed-in Minecraft account"
            };
            ownedCapeBox.SelectionChanged += OwnedCapeBox_SelectionChanged;
            controls.Children.Add(ownedCapeBox);

            var capeButtons = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(0, 9, 0, 0) };
            loadCapesButton = CreateAppearanceButton("LOAD OWNED", 104, LoadOwnedCapesButton_Click);
            applyCapeButton = CreateAppearanceButton("APPLY", 78, ApplyCapeButton_Click, accent: true);
            hideCapeButton = CreateAppearanceButton("HIDE", 72, HideCapeButton_Click);
            capeButtons.Children.Add(loadCapesButton);
            capeButtons.Children.Add(applyCapeButton);
            capeButtons.Children.Add(hideCapeButton);
            controls.Children.Add(capeButtons);

            importCapeButton = CreateAppearanceButton("IMPORT CAPE PNG", 148, ImportCapeButton_Click);
            importCapeButton.Margin = new Thickness(0, 8, 0, 0);
            importCapeButton.ToolTip = "Preview a custom 2:1 cape texture locally. Mojang does not allow arbitrary cape uploads to an account.";
            controls.Children.Add(importCapeButton);

            capeStatusText = new TextBlock
            {
                Text = "Load your account capes, or import a cape PNG for local 3D preview.",
                TextWrapping = TextWrapping.Wrap,
                Margin = new Thickness(0, 7, 0, 0),
                FontSize = 11
            };
            capeStatusText.SetResourceReference(TextBlock.ForegroundProperty, "Muted");
            controls.Children.Add(capeStatusText);
        }

        RenderAppearancePreview();
    }

    private Button CreateAppearanceButton(string text, double width, RoutedEventHandler handler, bool accent = false)
    {
        var button = new Button
        {
            Content = text,
            Width = width,
            Height = 34,
            Margin = new Thickness(0, 0, 8, 0),
            Padding = new Thickness(8, 0, 8, 0),
            Style = (Style)FindResource(accent ? "AccentButton" : "MazButton")
        };
        button.Click += handler;
        return button;
    }

    private void ImportSkinButton_Click(object sender, RoutedEventArgs e)
    {
        var picker = new OpenFileDialog
        {
            Filter = "Minecraft skin PNG (*.png)|*.png",
            Multiselect = false,
            Title = "Import Minecraft skin"
        };

        if (picker.ShowDialog() != true) return;

        try
        {
            var frame = LoadBitmapFromFile(picker.FileName);
            if (frame.PixelWidth != 64 || (frame.PixelHeight != 64 && frame.PixelHeight != 32))
                throw new InvalidDataException($"Minecraft skins must be 64x64 PNGs (or legacy 64x32). This file is {frame.PixelWidth}x{frame.PixelHeight}.");

            selectedSkinPath = picker.FileName;
            previewSkinImage = frame;
            SkinPreviewPlaceholder.Visibility = Visibility.Collapsed;
            SkinStatusText.Text = $"Selected: {Path.GetFileName(picker.FileName)} ({frame.PixelWidth}x{frame.PixelHeight})";
            StatusText.Text = "Skin ready to apply";
            RenderAppearancePreview();
        }
        catch (Exception ex)
        {
            selectedSkinPath = null;
            previewSkinImage = null;
            SkinPreviewPlaceholder.Visibility = Visibility.Visible;
            SkinStatusText.Text = "No skin selected.";
            RenderAppearancePreview();
            MessageBox.Show(ex.Message, "Could not import skin", MessageBoxButton.OK, MessageBoxImage.Warning);
        }
    }

    private void ImportCapeButton_Click(object sender, RoutedEventArgs e)
    {
        var picker = new OpenFileDialog
        {
            Filter = "Minecraft cape PNG (*.png)|*.png",
            Multiselect = false,
            Title = "Import Minecraft cape texture"
        };

        if (picker.ShowDialog() != true) return;

        try
        {
            var frame = LoadBitmapFromFile(picker.FileName);
            if (frame.PixelWidth < 64 || frame.PixelHeight < 32 || frame.PixelWidth != frame.PixelHeight * 2)
                throw new InvalidDataException($"Cape textures must use Minecraft's 2:1 layout (64x32, 128x64, 256x128, etc.). This file is {frame.PixelWidth}x{frame.PixelHeight}.");

            selectedCapePath = picker.FileName;
            selectedOwnedCape = null;
            if (ownedCapeBox != null) ownedCapeBox.SelectedItem = null;
            previewCapeImage = frame;
            if (capeStatusText != null)
                capeStatusText.Text = $"Previewing custom cape: {Path.GetFileName(picker.FileName)}. Custom PNGs are local preview only; Minecraft accounts can only activate owned capes.";
            StatusText.Text = "Custom cape loaded for 3D preview";
            RenderAppearancePreview();
        }
        catch (Exception ex)
        {
            selectedCapePath = null;
            previewCapeImage = null;
            RenderAppearancePreview();
            MessageBox.Show(ex.Message, "Could not import cape", MessageBoxButton.OK, MessageBoxImage.Warning);
        }
    }

    private async void LoadOwnedCapesButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null)
        {
            MessageBox.Show("Sign in to your Microsoft / Minecraft account first.", "Sign in required", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        try
        {
            SetCapeControlsEnabled(false);
            SetBusy(true, "Loading Minecraft appearance...");

            using var request = new HttpRequestMessage(HttpMethod.Get, "https://api.minecraftservices.com/minecraft/profile");
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", session.AccessToken);
            request.Headers.Accept.ParseAdd("application/json");
            using var response = await uiHttp.SendAsync(request);
            var json = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException($"Minecraft profile request failed ({(int)response.StatusCode}). {json}");

            using var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;

            if (selectedSkinPath == null && root.TryGetProperty("skins", out var skins) && skins.ValueKind == JsonValueKind.Array)
            {
                foreach (var skin in skins.EnumerateArray())
                {
                    if (!IsActive(skin)) continue;
                    if (skin.TryGetProperty("url", out var skinUrl) && !string.IsNullOrWhiteSpace(skinUrl.GetString()))
                    {
                        previewSkinImage = await LoadRemoteBitmapAsync(skinUrl.GetString()!);
                        SkinPreviewPlaceholder.Visibility = Visibility.Collapsed;
                    }
                    if (skin.TryGetProperty("variant", out var variant)) SetSkinVariant(variant.GetString());
                    break;
                }
            }

            var capes = new List<OwnedCape>();
            if (root.TryGetProperty("capes", out var capeArray) && capeArray.ValueKind == JsonValueKind.Array)
            {
                foreach (var cape in capeArray.EnumerateArray())
                {
                    var id = cape.TryGetProperty("id", out var idValue) ? idValue.GetString() : null;
                    var url = cape.TryGetProperty("url", out var urlValue) ? urlValue.GetString() : null;
                    if (string.IsNullOrWhiteSpace(id) || string.IsNullOrWhiteSpace(url)) continue;
                    var alias = cape.TryGetProperty("alias", out var aliasValue) && !string.IsNullOrWhiteSpace(aliasValue.GetString())
                        ? aliasValue.GetString()!
                        : "Minecraft Cape";
                    capes.Add(new OwnedCape { Id = id!, Alias = alias, Url = url!, Active = IsActive(cape) });
                }
            }

            if (ownedCapeBox != null)
            {
                ownedCapeBox.ItemsSource = capes;
                ownedCapeBox.IsEnabled = capes.Count > 0;
                var active = capes.FirstOrDefault(c => c.Active);
                if (active != null) ownedCapeBox.SelectedItem = active;
            }

            if (capes.Count == 0)
            {
                selectedOwnedCape = null;
                if (selectedCapePath == null) previewCapeImage = null;
                if (capeStatusText != null) capeStatusText.Text = "This Minecraft account does not currently own any capes.";
            }
            else if (capeStatusText != null)
            {
                capeStatusText.Text = $"Loaded {capes.Count} owned cape{(capes.Count == 1 ? string.Empty : "s")}. Select one to preview and apply.";
            }

            SkinStatusText.Text = selectedSkinPath == null ? "Loaded current account skin for 3D preview." : SkinStatusText.Text;
            StatusText.Text = "Minecraft appearance loaded";
            RenderAppearancePreview();
        }
        catch (Exception ex)
        {
            if (capeStatusText != null) capeStatusText.Text = "Could not load owned capes.";
            StatusText.Text = "Appearance load failed";
            MessageBox.Show(ex.Message, "Could not load Minecraft appearance", MessageBoxButton.OK, MessageBoxImage.Error);
        }
        finally
        {
            SetBusy(false);
            SetCapeControlsEnabled(true);
        }
    }

    private async void OwnedCapeBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (ownedCapeBox?.SelectedItem is not OwnedCape cape) return;

        selectedOwnedCape = cape;
        selectedCapePath = null;
        try
        {
            previewCapeImage = await LoadRemoteBitmapAsync(cape.Url);
            if (capeStatusText != null) capeStatusText.Text = $"Previewing owned cape: {cape.Alias}.";
            RenderAppearancePreview();
        }
        catch (Exception ex)
        {
            if (capeStatusText != null) capeStatusText.Text = $"Could not preview {cape.Alias}.";
            AddLauncherLog("Cape preview download failed: " + ex.Message);
        }
    }

    private async void ApplyCapeButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null)
        {
            MessageBox.Show("Sign in before applying a cape.", "Sign in required", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }
        if (selectedOwnedCape == null)
        {
            var detail = selectedCapePath != null
                ? "Custom cape PNGs can be previewed in MazLauncher, but Mojang does not allow arbitrary cape uploads. Select a cape your account owns to apply it."
                : "Load your owned Minecraft capes and select one first.";
            MessageBox.Show(detail, "No owned cape selected", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        try
        {
            SetCapeControlsEnabled(false);
            SetBusy(true, "Applying Minecraft cape...");
            var payload = JsonSerializer.Serialize(new { capeId = selectedOwnedCape.Id });
            using var request = new HttpRequestMessage(HttpMethod.Put, "https://api.minecraftservices.com/minecraft/profile/capes/active")
            {
                Content = new StringContent(payload, Encoding.UTF8, "application/json")
            };
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", session.AccessToken);
            request.Headers.Accept.ParseAdd("application/json");
            using var response = await uiHttp.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException($"Minecraft rejected the cape change ({(int)response.StatusCode}). {body}");

            if (ownedCapeBox?.ItemsSource is IEnumerable<OwnedCape> capes)
                foreach (var cape in capes) cape.Active = string.Equals(cape.Id, selectedOwnedCape.Id, StringComparison.OrdinalIgnoreCase);
            ownedCapeBox?.Items.Refresh();
            if (capeStatusText != null) capeStatusText.Text = $"Applied {selectedOwnedCape.Alias}.";
            StatusText.Text = "Cape updated successfully";
        }
        catch (Exception ex)
        {
            StatusText.Text = "Cape update failed";
            MessageBox.Show(ex.Message, "Could not apply cape", MessageBoxButton.OK, MessageBoxImage.Error);
        }
        finally
        {
            SetBusy(false);
            SetCapeControlsEnabled(true);
        }
    }

    private async void HideCapeButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null)
        {
            MessageBox.Show("Sign in before changing cape visibility.", "Sign in required", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        try
        {
            SetCapeControlsEnabled(false);
            SetBusy(true, "Hiding Minecraft cape...");
            using var request = new HttpRequestMessage(HttpMethod.Delete, "https://api.minecraftservices.com/minecraft/profile/capes/active");
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", session.AccessToken);
            using var response = await uiHttp.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException($"Minecraft rejected the cape change ({(int)response.StatusCode}). {body}");

            if (ownedCapeBox?.ItemsSource is IEnumerable<OwnedCape> capes)
                foreach (var cape in capes) cape.Active = false;
            ownedCapeBox?.Items.Refresh();
            if (capeStatusText != null) capeStatusText.Text = "Account cape hidden. You can still preview owned or imported cape textures here.";
            StatusText.Text = "Cape hidden";
        }
        catch (Exception ex)
        {
            StatusText.Text = "Cape update failed";
            MessageBox.Show(ex.Message, "Could not hide cape", MessageBoxButton.OK, MessageBoxImage.Error);
        }
        finally
        {
            SetBusy(false);
            SetCapeControlsEnabled(true);
        }
    }

    private async void ApplySkinButton_Click(object sender, RoutedEventArgs e)
    {
        if (session == null)
        {
            MessageBox.Show("Sign in to your Microsoft / Minecraft account before applying a skin.", "Sign in required", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        if (string.IsNullOrWhiteSpace(selectedSkinPath) || !File.Exists(selectedSkinPath))
        {
            MessageBox.Show("Import a skin PNG first.", "No skin selected", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var variant = (SkinModelBox.SelectedItem as ComboBoxItem)?.Tag?.ToString() ?? "classic";

        try
        {
            SetBusy(true, "Uploading skin to Minecraft...");
            ImportSkinButton.IsEnabled = false;
            ApplySkinButton.IsEnabled = false;

            using var form = new MultipartFormDataContent();
            form.Add(new StringContent(variant), "variant");

            await using var skinStream = File.OpenRead(selectedSkinPath);
            using var fileContent = new StreamContent(skinStream);
            fileContent.Headers.ContentType = new MediaTypeHeaderValue("image/png");
            form.Add(fileContent, "file", Path.GetFileName(selectedSkinPath));

            using var request = new HttpRequestMessage(HttpMethod.Post, "https://api.minecraftservices.com/minecraft/profile/skins")
            {
                Content = form
            };
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", session.AccessToken);
            request.Headers.Accept.ParseAdd("application/json");

            using var response = await uiHttp.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
            {
                var detail = string.IsNullOrWhiteSpace(responseBody) ? response.ReasonPhrase : responseBody;
                throw new InvalidOperationException($"Minecraft rejected the skin upload ({(int)response.StatusCode}). {detail}");
            }

            SkinStatusText.Text = $"Applied {Path.GetFileName(selectedSkinPath)} as {variant}.";
            StatusText.Text = "Skin updated successfully";
            MessageBox.Show("Your Minecraft skin was updated successfully.", "Skin applied", MessageBoxButton.OK, MessageBoxImage.Information);
        }
        catch (Exception ex)
        {
            SkinStatusText.Text = "Skin upload failed.";
            StatusText.Text = "Skin upload failed";
            MessageBox.Show(ex.Message, "Could not apply skin", MessageBoxButton.OK, MessageBoxImage.Error);
        }
        finally
        {
            SetBusy(false);
            ImportSkinButton.IsEnabled = true;
            ApplySkinButton.IsEnabled = true;
        }
    }

    private void SetCapeControlsEnabled(bool enabled)
    {
        if (loadCapesButton != null) loadCapesButton.IsEnabled = enabled;
        if (importCapeButton != null) importCapeButton.IsEnabled = enabled;
        if (applyCapeButton != null) applyCapeButton.IsEnabled = enabled;
        if (hideCapeButton != null) hideCapeButton.IsEnabled = enabled;
        if (ownedCapeBox != null) ownedCapeBox.IsEnabled = enabled && ownedCapeBox.Items.Count > 0;
    }

    private static bool IsActive(JsonElement item)
    {
        return item.TryGetProperty("state", out var state)
            && string.Equals(state.GetString(), "ACTIVE", StringComparison.OrdinalIgnoreCase);
    }

    private void SetSkinVariant(string? variant)
    {
        var target = string.Equals(variant, "SLIM", StringComparison.OrdinalIgnoreCase) ? "slim" : "classic";
        foreach (var item in SkinModelBox.Items.OfType<ComboBoxItem>())
        {
            if (string.Equals(item.Tag?.ToString(), target, StringComparison.OrdinalIgnoreCase))
            {
                SkinModelBox.SelectedItem = item;
                return;
            }
        }
    }

    private static BitmapFrame LoadBitmapFromFile(string path)
    {
        using var stream = File.OpenRead(path);
        var decoder = BitmapDecoder.Create(stream, BitmapCreateOptions.PreservePixelFormat, BitmapCacheOption.OnLoad);
        var frame = decoder.Frames[0];
        frame.Freeze();
        return frame;
    }

    private async Task<BitmapFrame> LoadRemoteBitmapAsync(string url)
    {
        var bytes = await uiHttp.GetByteArrayAsync(url);
        using var stream = new MemoryStream(bytes, writable: false);
        var decoder = BitmapDecoder.Create(stream, BitmapCreateOptions.PreservePixelFormat, BitmapCacheOption.OnLoad);
        var frame = decoder.Frames[0];
        frame.Freeze();
        return frame;
    }

    private void RenderAppearancePreview()
    {
        if (appearanceViewport == null) return;

        appearanceViewport.Children.Clear();
        var world = new Model3DGroup();
        world.Children.Add(new AmbientLight(Color.FromRgb(185, 195, 215)));
        world.Children.Add(new DirectionalLight(Colors.White, new Vector3D(-0.4, -0.7, -1)));

        var model = new Model3DGroup();
        var transform = new Transform3DGroup();
        transform.Children.Add(new RotateTransform3D(appearancePitch));
        transform.Children.Add(new RotateTransform3D(appearanceYaw));
        model.Transform = transform;

        var skinMaterial = CreateMaterial(previewSkinImage, Color.FromRgb(116, 129, 153));
        var slim = string.Equals((SkinModelBox.SelectedItem as ComboBoxItem)?.Tag?.ToString(), "slim", StringComparison.OrdinalIgnoreCase);
        var legacy = previewSkinImage is BitmapSource bitmap && bitmap.PixelHeight == 32;

        AddBox(model, new Point3D(0, 1.2, 0), 0.8, 0.8, 0.8, skinMaterial, CubeUv(0, 0, 8, 8, 8), 64, 64);
        if (previewSkinImage != null)
            AddBox(model, new Point3D(0, 1.2, 0), 0.825, 0.825, 0.825, skinMaterial, CubeUv(32, 0, 8, 8, 8), 64, 64);

        AddBox(model, new Point3D(0, 0.2, 0), 0.8, 1.2, 0.4, skinMaterial, CubeUv(16, 16, 8, 12, 4), 64, 64);

        var armWidthPixels = slim ? 3 : 4;
        var armWidth = armWidthPixels / 10.0;
        var armX = 0.4 + armWidth / 2.0;
        AddBox(model, new Point3D(-armX, 0.2, 0), armWidth, 1.2, 0.4, skinMaterial, CubeUv(40, 16, armWidthPixels, 12, 4), 64, 64);
        AddBox(model, new Point3D(armX, 0.2, 0), armWidth, 1.2, 0.4, skinMaterial,
            legacy ? CubeUv(40, 16, armWidthPixels, 12, 4) : CubeUv(32, 48, armWidthPixels, 12, 4), 64, 64);

        AddBox(model, new Point3D(-0.2, -1.0, 0), 0.4, 1.2, 0.4, skinMaterial, CubeUv(0, 16, 4, 12, 4), 64, 64);
        AddBox(model, new Point3D(0.2, -1.0, 0), 0.4, 1.2, 0.4, skinMaterial,
            legacy ? CubeUv(0, 16, 4, 12, 4) : CubeUv(16, 48, 4, 12, 4), 64, 64);

        if (previewCapeImage != null)
        {
            var capeMaterial = CreateMaterial(previewCapeImage, Colors.White);
            AddBox(model, new Point3D(0, 0.0, -0.27), 1.0, 1.6, 0.1, capeMaterial, CubeUv(0, 0, 10, 16, 1), 64, 32);
        }

        world.Children.Add(model);
        appearanceViewport.Children.Add(new ModelVisual3D { Content = world });
        SkinPreviewPlaceholder.Visibility = previewSkinImage == null ? Visibility.Visible : Visibility.Collapsed;
    }

    private static Material CreateMaterial(ImageSource? image, Color fallback)
    {
        Brush brush = image == null
            ? new SolidColorBrush(fallback)
            : new ImageBrush(image) { Stretch = Stretch.Fill, TileMode = TileMode.None };
        return new DiffuseMaterial(brush);
    }

    private static BoxUv CubeUv(double u, double v, double width, double height, double depth)
    {
        return new BoxUv(
            new UvRect(u + depth, v, width, depth),
            new UvRect(u + depth + width, v, width, depth),
            new UvRect(u, v + depth, depth, height),
            new UvRect(u + depth, v + depth, width, height),
            new UvRect(u + depth + width, v + depth, depth, height),
            new UvRect(u + depth + width + depth, v + depth, width, height));
    }

    private static void AddBox(Model3DGroup group, Point3D center, double width, double height, double depth,
        Material material, BoxUv uv, double textureWidth, double textureHeight)
    {
        var hx = width / 2.0;
        var hy = height / 2.0;
        var hz = depth / 2.0;
        var x0 = center.X - hx;
        var x1 = center.X + hx;
        var y0 = center.Y - hy;
        var y1 = center.Y + hy;
        var z0 = center.Z - hz;
        var z1 = center.Z + hz;
        var mesh = new MeshGeometry3D();

        AddQuad(mesh, new Point3D(x0, y1, z1), new Point3D(x0, y0, z1), new Point3D(x1, y0, z1), new Point3D(x1, y1, z1), uv.Front, textureWidth, textureHeight);
        AddQuad(mesh, new Point3D(x1, y1, z0), new Point3D(x1, y0, z0), new Point3D(x0, y0, z0), new Point3D(x0, y1, z0), uv.Back, textureWidth, textureHeight);
        AddQuad(mesh, new Point3D(x0, y1, z0), new Point3D(x0, y0, z0), new Point3D(x0, y0, z1), new Point3D(x0, y1, z1), uv.Right, textureWidth, textureHeight);
        AddQuad(mesh, new Point3D(x1, y1, z1), new Point3D(x1, y0, z1), new Point3D(x1, y0, z0), new Point3D(x1, y1, z0), uv.Left, textureWidth, textureHeight);
        AddQuad(mesh, new Point3D(x0, y1, z0), new Point3D(x0, y1, z1), new Point3D(x1, y1, z1), new Point3D(x1, y1, z0), uv.Top, textureWidth, textureHeight);
        AddQuad(mesh, new Point3D(x0, y0, z1), new Point3D(x0, y0, z0), new Point3D(x1, y0, z0), new Point3D(x1, y0, z1), uv.Bottom, textureWidth, textureHeight);

        group.Children.Add(new GeometryModel3D(mesh, material) { BackMaterial = material });
    }

    private static void AddQuad(MeshGeometry3D mesh, Point3D topLeft, Point3D bottomLeft, Point3D bottomRight, Point3D topRight,
        UvRect uv, double textureWidth, double textureHeight)
    {
        var start = mesh.Positions.Count;
        mesh.Positions.Add(topLeft);
        mesh.Positions.Add(bottomLeft);
        mesh.Positions.Add(bottomRight);
        mesh.Positions.Add(topRight);
        mesh.TextureCoordinates.Add(new Point(uv.X / textureWidth, uv.Y / textureHeight));
        mesh.TextureCoordinates.Add(new Point(uv.X / textureWidth, (uv.Y + uv.Height) / textureHeight));
        mesh.TextureCoordinates.Add(new Point((uv.X + uv.Width) / textureWidth, (uv.Y + uv.Height) / textureHeight));
        mesh.TextureCoordinates.Add(new Point((uv.X + uv.Width) / textureWidth, uv.Y / textureHeight));
        mesh.TriangleIndices.Add(start);
        mesh.TriangleIndices.Add(start + 1);
        mesh.TriangleIndices.Add(start + 2);
        mesh.TriangleIndices.Add(start);
        mesh.TriangleIndices.Add(start + 2);
        mesh.TriangleIndices.Add(start + 3);
    }

    private void AppearanceViewport_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
    {
        if (appearanceViewport == null) return;
        appearanceDragStart = e.GetPosition(appearanceViewport);
        appearanceDragYaw = appearanceYaw.Angle;
        appearanceDragPitch = appearancePitch.Angle;
        appearanceViewport.CaptureMouse();
    }

    private void AppearanceViewport_MouseMove(object sender, MouseEventArgs e)
    {
        if (appearanceViewport == null || !appearanceViewport.IsMouseCaptured || e.LeftButton != MouseButtonState.Pressed) return;
        var point = e.GetPosition(appearanceViewport);
        appearanceYaw.Angle = appearanceDragYaw + (point.X - appearanceDragStart.X) * 0.55;
        appearancePitch.Angle = Math.Clamp(appearanceDragPitch - (point.Y - appearanceDragStart.Y) * 0.35, -30, 30);
    }

    private void AppearanceViewport_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
    {
        appearanceViewport?.ReleaseMouseCapture();
    }

    private void AppearanceViewport_MouseWheel(object sender, MouseWheelEventArgs e)
    {
        if (appearanceCamera == null) return;
        var z = Math.Clamp(appearanceCamera.Position.Z - Math.Sign(e.Delta) * 0.35, 3.5, 7.0);
        appearanceCamera.Position = new Point3D(0, 0.05, z);
        appearanceCamera.LookDirection = new Vector3D(0, 0, -z);
    }
}
