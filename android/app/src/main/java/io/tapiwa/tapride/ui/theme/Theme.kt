package io.tapiwa.tapride.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue600,
    onPrimary = White,
    primaryContainer = Blue800,
    secondary = Green600,
    onSecondary = White,
    secondaryContainer = Green800,
    background = Gray50,
    surface = White,
    onBackground = Gray900,
    onSurface = Gray900,
    error = ErrorRed,
    onError = White
)

private val DarkColors = darkColorScheme(
    primary = Blue600,
    onPrimary = White,
    primaryContainer = Blue800,
    secondary = Green600,
    onSecondary = White,
    secondaryContainer = Green800,
    background = Gray900,
    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    onBackground = White,
    onSurface = White,
    error = ErrorRed,
    onError = White
)

/**
 * Top-level Material 3 theme for TapRide.
 *
 * Automatically switches between [LightColors] and [DarkColors] based on
 * the system dark-mode setting.
 *
 * @param darkTheme  Force dark theme; defaults to system preference.
 * @param content    Composable content to render within the theme.
 */
@Composable
fun TapRideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = TapRideTypography,
        content = content
    )
}
