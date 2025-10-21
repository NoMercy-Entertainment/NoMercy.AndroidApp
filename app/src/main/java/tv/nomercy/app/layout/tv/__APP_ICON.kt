package tv.nomercy.app.components.brand

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import tv.nomercy.app.layout.tv.app_icon.IcLauncherForeground
import kotlin.collections.List as ____KtList

object APP_ICON

private var __AllIcons: ____KtList<ImageVector>? = null

val APP_ICON.AllIcons: ____KtList<ImageVector>
  @Composable
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(IcLauncherForeground,IcLauncherMonochrome)
    return __AllIcons!!
  }
