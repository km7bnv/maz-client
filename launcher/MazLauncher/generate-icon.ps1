$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$outDir = Join-Path $PSScriptRoot 'Assets'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$outPath = Join-Path $outDir 'MazLauncher.ico'

$sizes = @(16, 24, 32, 48, 64, 128, 256)
$images = @()

foreach ($size in $sizes) {
    $bitmap = New-Object System.Drawing.Bitmap($size, $size)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.Clear([System.Drawing.Color]::Transparent)

    $radius = [Math]::Max(2, [int]($size * 0.17))
    $diameter = $radius * 2
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddArc(0, 0, $diameter, $diameter, 180, 90)
    $path.AddArc($size - $diameter, 0, $diameter, $diameter, 270, 90)
    $path.AddArc($size - $diameter, $size - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc(0, $size - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()

    $purpleBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 88, 101, 242))
    $graphics.FillPath($purpleBrush, $path)

    $fontSize = [Math]::Max(9, [single]($size * 0.60))
    $font = New-Object System.Drawing.Font('Arial', $fontSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $format = New-Object System.Drawing.StringFormat
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $graphics.DrawString('M', $font, $whiteBrush, (New-Object System.Drawing.RectangleF(0, -($size * 0.02), $size, $size)), $format)

    $pngStream = New-Object System.IO.MemoryStream
    $bitmap.Save($pngStream, [System.Drawing.Imaging.ImageFormat]::Png)
    $images += [PSCustomObject]@{ Size = $size; Bytes = $pngStream.ToArray() }

    $pngStream.Dispose()
    $format.Dispose()
    $whiteBrush.Dispose()
    $font.Dispose()
    $purpleBrush.Dispose()
    $path.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

$file = [System.IO.File]::Create($outPath)
$writer = New-Object System.IO.BinaryWriter($file)
try {
    # ICONDIR
    $writer.Write([UInt16]0)
    $writer.Write([UInt16]1)
    $writer.Write([UInt16]$images.Count)

    $offset = 6 + (16 * $images.Count)
    foreach ($image in $images) {
        $dimension = if ($image.Size -eq 256) { 0 } else { $image.Size }
        $writer.Write([Byte]$dimension)
        $writer.Write([Byte]$dimension)
        $writer.Write([Byte]0)
        $writer.Write([Byte]0)
        $writer.Write([UInt16]1)
        $writer.Write([UInt16]32)
        $writer.Write([UInt32]$image.Bytes.Length)
        $writer.Write([UInt32]$offset)
        $offset += $image.Bytes.Length
    }

    foreach ($image in $images) {
        $writer.Write([Byte[]]$image.Bytes)
    }
}
finally {
    $writer.Dispose()
    $file.Dispose()
}

Write-Host "Generated multi-resolution MazLauncher icon: $outPath ($($sizes -join ', ') px)"
