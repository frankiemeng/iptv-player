package com.iptv.player.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0F0F1A)
val DarkSurface = Color(0xFF1C1C2E)
val DarkSurfaceVariant = Color(0xFF2A2A3E)
val PrimaryBlue = Color(0xFF4A9EFF)
val SecondaryOrange = Color(0xFFFF7A30)
val TextPrimary = Color(0xFFF0F0F8)
val TextSecondary = Color(0xFF9E9EB8)
val ErrorRed = Color(0xFFEF5350)
val SuccessGreen = Color(0xFF66BB6A)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue, secondary = SecondaryOrange,
    background = DarkBackground, surface = DarkSurface, surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White, onSecondary = Color.White,
    onBackground = TextPrimary, onSurface = TextPrimary, onSurfaceVariant = TextSecondary,
)

@Composable
fun IPTVPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}
