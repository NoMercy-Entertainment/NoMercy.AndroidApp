package tv.nomercy.app.components.music

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName

@Composable
fun DeviceButton(
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    MusicButton(
        onClick = {
            isMenuOpen = !isMenuOpen
            // TODO: Toggle device menu
        },
        label = "Playing on this device",
        modifier = modifier,
    ) {
        MoooomIcon(
            icon = MoooomIconName.Devices,
            contentDescription = "Devices",
            tint = if (isMenuOpen) activeColor else Color.White
        )
    }
}
