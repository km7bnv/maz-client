using System.IO;
using System.Net.Http.Headers;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Imaging;
using Microsoft.Win32;

namespace MazLauncher;

public partial class MainWindow
{
    private string? selectedSkinPath;

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
            using var stream = File.OpenRead(picker.FileName);
            var decoder = BitmapDecoder.Create(stream, BitmapCreateOptions.PreservePixelFormat, BitmapCacheOption.OnLoad);
            var frame = decoder.Frames[0];
            if (frame.PixelWidth != 64 || (frame.PixelHeight != 64 && frame.PixelHeight != 32))
                throw new InvalidDataException($"Minecraft skins must be 64x64 PNGs (or legacy 64x32). This file is {frame.PixelWidth}x{frame.PixelHeight}.");

            selectedSkinPath = picker.FileName;
            SkinPreview.Source = frame;
            SkinPreviewPlaceholder.Visibility = Visibility.Collapsed;
            SkinStatusText.Text = $"Selected: {Path.GetFileName(picker.FileName)} ({frame.PixelWidth}x{frame.PixelHeight})";
            StatusText.Text = "Skin ready to apply";
        }
        catch (Exception ex)
        {
            selectedSkinPath = null;
            SkinPreview.Source = null;
            SkinPreviewPlaceholder.Visibility = Visibility.Visible;
            SkinStatusText.Text = "No skin selected.";
            MessageBox.Show(ex.Message, "Could not import skin", MessageBoxButton.OK, MessageBoxImage.Warning);
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
}
