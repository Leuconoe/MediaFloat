package com.mediacontrol.floatingwidget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = MossGreen,
    onPrimary = WarmStone,
    primaryContainer = SageGreen,
    onPrimaryContainer = WarmStone,
    secondary = ClayAccent,
    onSecondary = WarmStone,
    secondaryContainer = Color(0xFFEFD8CF),
    onSecondaryContainer = SlateInk,
    tertiary = Color(0xFF8B9D83),
    onTertiary = SlateInk,
    tertiaryContainer = Color(0xFFDCE5D6),
    onTertiaryContainer = SlateInk,
    background = SandSurface,
    onBackground = SlateInk,
    surface = WarmStone,
    onSurface = SlateInk,
    surfaceVariant = Color(0xFFE3DDD2),
    onSurfaceVariant = Color(0xFF4D564F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC0D2C0),
    onPrimary = Color(0xFF243027),
    primaryContainer = Color(0xFF39503D),
    onPrimaryContainer = WarmStone,
    secondary = Color(0xFFF0B59B),
    onSecondary = Color(0xFF4D2614),
    secondaryContainer = Color(0xFF6A3A24),
    onSecondaryContainer = WarmStone,
    tertiary = Color(0xFFBCCDB4),
    onTertiary = Color(0xFF263122),
    tertiaryContainer = Color(0xFF3D4A39),
    onTertiaryContainer = WarmStone,
    background = Color(0xFF121512),
    onBackground = Color(0xFFE5E2DA),
    surface = Color(0xFF191C19),
    onSurface = Color(0xFFE5E2DA),
    surfaceVariant = Color(0xFF42493F),
    onSurfaceVariant = Color(0xFFC4C9BE)
)

@Composable
fun MediaFloatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
