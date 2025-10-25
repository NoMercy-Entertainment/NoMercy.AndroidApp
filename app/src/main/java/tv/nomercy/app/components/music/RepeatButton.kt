package tv.nomercy.app.components.music

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.stores.musicPlayer.RepeatState

@Composable
fun RepeatButton(
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val repeatState = musicPlayerStore.repeatState.collectAsState().value

    MusicButton(
        onClick = {
            val nextState = when (repeatState) {
                RepeatState.OFF -> RepeatState.ALL
                RepeatState.ALL -> RepeatState.ONE
                RepeatState.ONE -> RepeatState.OFF
            }
            musicPlayerStore.setRepeat(nextState)
        },
        label = "Repeat",
        modifier = modifier,
    ) {
        MoooomIcon(
            icon = when (repeatState) {
                RepeatState.OFF -> MoooomIconName.NmRepeatHalftone
                RepeatState.ALL -> MoooomIconName.NmRepeatSolid
                RepeatState.ONE -> MoooomIconName.NmRepeatOneSolid
            },
            contentDescription = "Repeat",
            tint = if (repeatState != RepeatState.OFF) activeColor else Color.White
        )
    }
}