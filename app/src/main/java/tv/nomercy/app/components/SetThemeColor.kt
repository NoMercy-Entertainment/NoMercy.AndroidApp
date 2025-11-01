package tv.nomercy.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.ui.ThemeName
import tv.nomercy.app.views.profile.themeColors
import java.util.UUID

object ThemeManager {
    var currentThemeColor: Color = themeColors.getValue(ThemeName.Crimson)
}

@Composable
fun SetThemeColor(
    color: Color? = null
) {
    val themeOverrideManager = LocalThemeOverrideManager.current

    val themeColor: Color = color ?: ThemeManager.currentThemeColor
    val key = remember { UUID.randomUUID() }

    DisposableEffect(themeColor) {
        themeOverrideManager.add(key, themeColor)

        onDispose {
            themeOverrideManager.remove(key)
        }
    }
}