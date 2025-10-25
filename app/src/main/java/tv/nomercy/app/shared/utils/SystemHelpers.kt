package tv.nomercy.app.shared.utils

import android.app.UiModeManager
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun isTv(): Boolean {
    val context = LocalContext.current
    val uiModeManager = context.getSystemService(UiModeManager::class.java)
    return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
}