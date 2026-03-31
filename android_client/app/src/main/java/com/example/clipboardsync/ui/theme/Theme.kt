package com.example.clipboardsync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = SurfaceLight,
    secondary = InkBlue,
    onSecondary = SurfaceLight,
    background = DarkSurface,
    onBackground = SurfaceLight,
    surface = DarkCard,
    onSurface = SurfaceLight,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = BorderSoft,
    outline = DarkBorder,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = InkBlue,
    onPrimary = SurfaceLight,
    secondary = TealAccent,
    onSecondary = SurfaceLight,
    background = Mist,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE7EFF2),
    onSurfaceVariant = MutedText,
    outline = BorderSoft,
    error = ErrorRed
)

@Composable
fun ClipboardSyncTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
