package tv.nomercy.app.shared.components.music

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import tv.nomercy.app.shared.components.MoooomIcon
import tv.nomercy.app.shared.components.MoooomIconName
import tv.nomercy.app.shared.stores.GlobalStores

@Composable
fun PlaybackButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val isPlaying = musicPlayerStore.isPlaying.collectAsState().value

    MusicButton(
        onClick = {
            musicPlayerStore.togglePlayback()
        },
        label = if (isPlaying) "Pause" else "Play",
        modifier = modifier,
    ) {
        MoooomIcon(
            icon = if (isPlaying) MoooomIconName.NmPause else MoooomIconName.NmPlay,
            contentDescription = if (isPlaying) "Pause" else "Play",
        )
    }
}
