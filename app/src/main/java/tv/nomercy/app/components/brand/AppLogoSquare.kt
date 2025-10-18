package tv.nomercy.app.shared.components.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * App logo component that renders the theme-aware launcher foreground vector.
 * This uses a themable ImageVector rather than the static mipmap to better integrate with dynamic themes.
 */
@Composable
fun AppLogoSquare(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    contentDescription: String? = null,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Image(imageVector = IcLauncherForeground, contentDescription = contentDescription)
    }
}
