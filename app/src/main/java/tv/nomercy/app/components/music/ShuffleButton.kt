package tv.nomercy.app.components.music

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.stores.GlobalStores

@Composable
fun ShuffleButton(
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val isShuffling = musicPlayerStore.isShuffling.collectAsState().value

    MusicButton(
        onClick = {
            musicPlayerStore.setShuffle(!isShuffling)
        },
        label = "Shuffle",
        modifier = modifier,
    ) {
        MoooomIcon(
            icon = MoooomIconName.NmShuffleSolid,
            contentDescription = "Shuffle",
            tint = if (isShuffling) activeColor else Color.White
        )
    }
}