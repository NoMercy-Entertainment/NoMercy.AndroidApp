package tv.nomercy.app.shared.ui

import android.app.Activity
import android.graphics.Color
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

enum class AppThemeMode { LIGHT, DARK }

object SystemThemeController {

    fun applyThemeMode(activity: Activity, mode: AppThemeMode) {
        val isLight = mode == AppThemeMode.LIGHT

        // Let content draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)

        // Set system bar icon appearance
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = isLight
        controller.isAppearanceLightNavigationBars = isLight

        // Optional: set bar colors to match theme
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = Color.TRANSPARENT
    }
}

@Composable
fun AppThemeInitializer(mode: AppThemeMode) {
    val activity = LocalActivity.current as Activity
    SideEffect {
        SystemThemeController.applyThemeMode(activity, mode)
    }
}