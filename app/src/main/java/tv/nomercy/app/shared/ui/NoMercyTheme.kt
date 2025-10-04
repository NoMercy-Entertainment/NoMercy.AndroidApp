package tv.nomercy.app.shared.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun NoMercyTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = NoMercyTypography,
        content = content
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6200FF),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFFFF0266),
    background = Color(0xFF121212) // Adding a background color
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200FF),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFFFF0266)
)