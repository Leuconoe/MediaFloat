Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

function New-Color {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Hex,
        [int] $Alpha = 255
    )

    $base = [System.Drawing.ColorTranslator]::FromHtml($Hex)
    return [System.Drawing.Color]::FromArgb($Alpha, $base.R, $base.G, $base.B)
}

function New-RoundedRectPath {
    param(
        [float] $X,
        [float] $Y,
        [float] $Width,
        [float] $Height,
        [float] $Radius
    )

    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $diameter = $Radius * 2

    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()

    return $path
}

function Fill-RoundedRect {
    param(
        [System.Drawing.Graphics] $Graphics,
        [System.Drawing.Brush] $Brush,
        [float] $X,
        [float] $Y,
        [float] $Width,
        [float] $Height,
        [float] $Radius
    )

    $path = New-RoundedRectPath -X $X -Y $Y -Width $Width -Height $Height -Radius $Radius
    try {
        $Graphics.FillPath($Brush, $path)
    }
    finally {
        $path.Dispose()
    }
}

function Draw-RoundedRectBorder {
    param(
        [System.Drawing.Graphics] $Graphics,
        [System.Drawing.Pen] $Pen,
        [float] $X,
        [float] $Y,
        [float] $Width,
        [float] $Height,
        [float] $Radius
    )

    $path = New-RoundedRectPath -X $X -Y $Y -Width $Width -Height $Height -Radius $Radius
    try {
        $Graphics.DrawPath($Pen, $path)
    }
    finally {
        $path.Dispose()
    }
}

$width = 1024
$height = 500

$googlePlayDir = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path

$distributionOutput = Join-Path $googlePlayDir 'feature-graphic.png'
$fastlaneOutput = Join-Path $repoRoot 'fastlane\metadata\android\en-US\images\featureGraphic.png'

$bitmap = [System.Drawing.Bitmap]::new($width, $height)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)

try {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit

    $backgroundRect = [System.Drawing.Rectangle]::new(0, 0, $width, $height)
    $backgroundBrush = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        $backgroundRect,
        (New-Color '#EEF1E7'),
        (New-Color '#E4D1BD'),
        12.0
    )
    $graphics.FillRectangle($backgroundBrush, $backgroundRect)

    $mistBrush = [System.Drawing.SolidBrush]::new((New-Color '#FFF7EF' 95))
    $graphics.FillEllipse($mistBrush, 28, 20, 320, 170)
    $mistBrush.Dispose()

    $sageMistBrush = [System.Drawing.SolidBrush]::new((New-Color '#DCE6D6' 220))
    $graphics.FillEllipse($sageMistBrush, 566, 18, 388, 266)
    $graphics.FillEllipse($sageMistBrush, 662, 248, 246, 146)
    $sageMistBrush.Dispose()

    $sandAccentBrush = [System.Drawing.SolidBrush]::new((New-Color '#E7C896' 150))
    $graphics.FillEllipse($sandAccentBrush, 846, 56, 132, 132)
    $sandAccentBrush.Dispose()

    $terracottaGlowBrush = [System.Drawing.SolidBrush]::new((New-Color '#C97C5D' 70))
    $graphics.FillEllipse($terracottaGlowBrush, 856, 346, 168, 126)
    $terracottaGlowBrush.Dispose()

    $titleFont = [System.Drawing.Font]::new('Segoe UI Semibold', 68, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $subtitleFont = [System.Drawing.Font]::new('Segoe UI', 28, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $supportFont = [System.Drawing.Font]::new('Segoe UI', 22, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $panelLabelFont = [System.Drawing.Font]::new('Segoe UI Semibold', 16, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)

    $titleBrush = [System.Drawing.SolidBrush]::new((New-Color '#253129'))
    $subtitleBrush = [System.Drawing.SolidBrush]::new((New-Color '#415246'))
    $buttonTextBrush = [System.Drawing.SolidBrush]::new((New-Color '#334136'))
    $textFormat = [System.Drawing.StringFormat]::new()
    $textFormat.Alignment = [System.Drawing.StringAlignment]::Near
    $textFormat.LineAlignment = [System.Drawing.StringAlignment]::Near

    $graphics.DrawString('MediaFloat', $titleFont, $titleBrush, [System.Drawing.RectangleF]::new(86, 126, 470, 88), $textFormat)
    $graphics.DrawString('Floating media controls for Android', $subtitleFont, $subtitleBrush, [System.Drawing.RectangleF]::new(90, 224, 470, 42), $textFormat)
    $graphics.DrawString('Fast launch and stop shortcuts', $supportFont, $subtitleBrush, [System.Drawing.RectangleF]::new(90, 272, 430, 34), $textFormat)

    $panelShadowBrush = [System.Drawing.SolidBrush]::new((New-Color '#233027' 24))
    Fill-RoundedRect -Graphics $graphics -Brush $panelShadowBrush -X 586 -Y 98 -Width 336 -Height 304 -Radius 42
    $panelShadowBrush.Dispose()

    $panelBrush = [System.Drawing.SolidBrush]::new((New-Color '#FBF7F1'))
    Fill-RoundedRect -Graphics $graphics -Brush $panelBrush -X 580 -Y 88 -Width 336 -Height 304 -Radius 42
    $panelBrush.Dispose()

    $panelBorderPen = [System.Drawing.Pen]::new((New-Color '#E5D9CC'), 2)
    Draw-RoundedRectBorder -Graphics $graphics -Pen $panelBorderPen -X 580 -Y 88 -Width 336 -Height 304 -Radius 42
    $panelBorderPen.Dispose()

    $tabBaseBrush = [System.Drawing.SolidBrush]::new((New-Color '#F1ECE4'))
    Fill-RoundedRect -Graphics $graphics -Brush $tabBaseBrush -X 606 -Y 114 -Width 76 -Height 30 -Radius 15
    Fill-RoundedRect -Graphics $graphics -Brush $tabBaseBrush -X 692 -Y 114 -Width 92 -Height 30 -Radius 15
    Fill-RoundedRect -Graphics $graphics -Brush $tabBaseBrush -X 794 -Y 114 -Width 74 -Height 30 -Radius 15
    $tabBaseBrush.Dispose()

    $tabActiveBrush = [System.Drawing.SolidBrush]::new((New-Color '#D7E2D3'))
    Fill-RoundedRect -Graphics $graphics -Brush $tabActiveBrush -X 692 -Y 114 -Width 92 -Height 30 -Radius 15
    $tabActiveBrush.Dispose()

    $lineBrush = [System.Drawing.SolidBrush]::new((New-Color '#E3D7CA'))
    Fill-RoundedRect -Graphics $graphics -Brush $lineBrush -X 606 -Y 162 -Width 262 -Height 10 -Radius 5
    Fill-RoundedRect -Graphics $graphics -Brush $lineBrush -X 606 -Y 178 -Width 198 -Height 10 -Radius 5
    $lineBrush.Dispose()

    $statusCardBrush = [System.Drawing.SolidBrush]::new((New-Color '#D6E1D3'))
    Fill-RoundedRect -Graphics $graphics -Brush $statusCardBrush -X 606 -Y 200 -Width 282 -Height 78 -Radius 24
    $statusCardBrush.Dispose()

    $graphics.DrawString('Overlay controls', $panelLabelFont, $buttonTextBrush, [System.Drawing.PointF]::new(626, 220))

    $statusLineBrush = [System.Drawing.SolidBrush]::new((New-Color '#F6F1EA'))
    Fill-RoundedRect -Graphics $graphics -Brush $statusLineBrush -X 626 -Y 248 -Width 164 -Height 10 -Radius 5
    Fill-RoundedRect -Graphics $graphics -Brush $statusLineBrush -X 800 -Y 248 -Width 56 -Height 10 -Radius 5
    $statusLineBrush.Dispose()

    $launchBrush = [System.Drawing.SolidBrush]::new((New-Color '#EFF4EA'))
    Fill-RoundedRect -Graphics $graphics -Brush $launchBrush -X 606 -Y 312 -Width 130 -Height 46 -Radius 23
    $launchBrush.Dispose()

    $launchBorderPen = [System.Drawing.Pen]::new((New-Color '#C6D4C1'), 2)
    Draw-RoundedRectBorder -Graphics $graphics -Pen $launchBorderPen -X 606 -Y 312 -Width 130 -Height 46 -Radius 23
    $launchBorderPen.Dispose()

    $stopBrush = [System.Drawing.SolidBrush]::new((New-Color '#F4E4DB'))
    Fill-RoundedRect -Graphics $graphics -Brush $stopBrush -X 748 -Y 312 -Width 140 -Height 46 -Radius 23
    $stopBrush.Dispose()

    $stopBorderPen = [System.Drawing.Pen]::new((New-Color '#D9A892'), 2)
    Draw-RoundedRectBorder -Graphics $graphics -Pen $stopBorderPen -X 748 -Y 312 -Width 140 -Height 46 -Radius 23
    $stopBorderPen.Dispose()

    $launchIcon = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $launchIcon.AddPolygon([System.Drawing.PointF[]] @(
        [System.Drawing.PointF]::new(660, 325),
        [System.Drawing.PointF]::new(660, 345),
        [System.Drawing.PointF]::new(678, 335)
    ))
    $graphics.FillPath($buttonTextBrush, $launchIcon)
    $launchIcon.Dispose()

    Fill-RoundedRect -Graphics $graphics -Brush $buttonTextBrush -X 808 -Y 325 -Width 18 -Height 18 -Radius 5

    $overlayShadowBrush = [System.Drawing.SolidBrush]::new((New-Color '#1C241F' 22))
    Fill-RoundedRect -Graphics $graphics -Brush $overlayShadowBrush -X 620 -Y 208 -Width 268 -Height 92 -Radius 46
    Fill-RoundedRect -Graphics $graphics -Brush $overlayShadowBrush -X 624 -Y 214 -Width 268 -Height 92 -Radius 46
    $overlayShadowBrush.Dispose()

    $overlayBrush = [System.Drawing.SolidBrush]::new((New-Color '#2C3430'))
    Fill-RoundedRect -Graphics $graphics -Brush $overlayBrush -X 616 -Y 200 -Width 268 -Height 92 -Radius 46
    $overlayBrush.Dispose()

    $wellBrush = [System.Drawing.SolidBrush]::new((New-Color '#3B4540'))
    $graphics.FillEllipse($wellBrush, 634, 216, 58, 58)
    $graphics.FillEllipse($wellBrush, 806, 216, 58, 58)
    $wellBrush.Dispose()

    $centerWellBrush = [System.Drawing.SolidBrush]::new((New-Color '#46524B'))
    $graphics.FillEllipse($centerWellBrush, 718, 210, 70, 70)
    $centerWellBrush.Dispose()

    $iconBrush = [System.Drawing.SolidBrush]::new((New-Color '#FFF7EF'))

    $prevBar = [System.Drawing.RectangleF]::new(652, 229, 5, 32)
    $graphics.FillRectangle($iconBrush, $prevBar)
    $prevIcon = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $prevIcon.AddPolygon([System.Drawing.PointF[]] @(
        [System.Drawing.PointF]::new(677, 245),
        [System.Drawing.PointF]::new(660, 233),
        [System.Drawing.PointF]::new(660, 257)
    ))
    $graphics.FillPath($iconBrush, $prevIcon)
    $prevIcon.Dispose()

    $playIcon = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $playIcon.AddPolygon([System.Drawing.PointF[]] @(
        [System.Drawing.PointF]::new(744, 228),
        [System.Drawing.PointF]::new(744, 262),
        [System.Drawing.PointF]::new(771, 245)
    ))
    $graphics.FillPath($iconBrush, $playIcon)
    $playIcon.Dispose()

    $nextIcon = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $nextIcon.AddPolygon([System.Drawing.PointF[]] @(
        [System.Drawing.PointF]::new(823, 233),
        [System.Drawing.PointF]::new(823, 257),
        [System.Drawing.PointF]::new(840, 245)
    ))
    $graphics.FillPath($iconBrush, $nextIcon)
    $graphics.FillRectangle($iconBrush, [System.Drawing.RectangleF]::new(842, 229, 5, 32))
    $nextIcon.Dispose()

    $iconBrush.Dispose()

    $anchorBrush = [System.Drawing.SolidBrush]::new((New-Color '#C97C5D'))
    $graphics.FillEllipse($anchorBrush, 880, 220, 26, 26)
    $anchorBrush.Dispose()

    $backgroundBrush.Dispose()
    $textFormat.Dispose()

    $titleFont.Dispose()
    $subtitleFont.Dispose()
    $supportFont.Dispose()
    $panelLabelFont.Dispose()

    $titleBrush.Dispose()
    $subtitleBrush.Dispose()
    $buttonTextBrush.Dispose()
    $bitmap.Save($distributionOutput, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Save($fastlaneOutput, [System.Drawing.Imaging.ImageFormat]::Png)
}
finally {
    $graphics.Dispose()
    $bitmap.Dispose()
}
