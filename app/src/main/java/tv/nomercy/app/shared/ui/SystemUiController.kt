package tv.nomercy.app.shared.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.WindowInsets
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import tv.nomercy.app.shared.stores.ColorScheme
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.isColorLight

object SystemUiController {

    val statusBar = WindowInsets.Type.statusBars()
    val navigationBar = WindowInsets.Type.navigationBars()

    private fun setDecorFitsSystemWindows(activity: Activity, fits: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, fits)
    }

    fun setEdgeToEdge(activity: Activity) {
        setDecorFitsSystemWindows(activity, false)
    }

    fun enableImmersiveMode(activity: Activity) {
        setDecorFitsSystemWindows(activity, false)

        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.hide(statusBar or navigationBar)

        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun disableImmersiveMode(activity: Activity) {
        setDecorFitsSystemWindows(activity, true)

        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.show(statusBar or navigationBar)


        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }

    fun lockOrientationPortrait(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun lockOrientationLandscape(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    fun unlockOrientation(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun hideStatusBar(activity: Activity) {
        WindowInsetsControllerCompat(
            activity.window,
            activity.window.decorView
        ).hide(statusBar)
    }

    fun showStatusBar(activity: Activity) {
        WindowInsetsControllerCompat(
            activity.window,
            activity.window.decorView
        ).show(statusBar)
    }

    fun hideNavigationBar(activity: Activity) {
        WindowInsetsControllerCompat(
            activity.window,
            activity.window.decorView
        ).hide(navigationBar)
    }

    fun showNavigationBar(activity: Activity) {
        WindowInsetsControllerCompat(
            activity.window,
            activity.window.decorView
        ).show(navigationBar)
    }

    fun resetSystemBars(activity: Activity) {
        setDecorFitsSystemWindows(activity, true)
        showStatusBar(activity)
        showNavigationBar(activity)
        unlockOrientation(activity)
    }

    fun adjustBrightness(color: Color, targetLuminance: Float): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        hsl[2] = targetLuminance.coerceIn(0f, 1f) // Luminance
        val adjusted = ColorUtils.HSLToColor(hsl)
        return Color(adjusted)
    }


    fun setStatusBarColor(activity: Activity, color: Color) {
        val isDarkTheme = when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }

        val targetLuminance = if (isDarkTheme) 0.2f else 0.8f
        val adjustedColor = adjustBrightness(color, targetLuminance)

        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = !isDarkTheme

        activity.window.statusBarColor = adjustedColor.toArgb()
    }

//    fun setStatusBarColor(activity: Activity, color: Color) {
//        val hexColor = String.format("#%06X", (0xFFFFFF and color.toArgb()))
//        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
//            .isAppearanceLightStatusBars = isColorLight(hexColor)
//
//        activity.window.statusBarColor = color.toArgb()
//    }

    fun setStatusBarIconsLight(activity: Activity, isLight: Boolean) {
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = isLight
    }
}
