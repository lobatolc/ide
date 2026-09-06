package br.com.ide.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = IdePrimary,
    secondary = IdeSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = IdePrimaryDark,
    secondary = IdeSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = ErrorRed
)

@Composable
fun IdeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}