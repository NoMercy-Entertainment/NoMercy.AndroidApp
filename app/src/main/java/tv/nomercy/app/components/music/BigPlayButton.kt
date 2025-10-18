package tv.nomercy.app.shared.components.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import tv.nomercy.app.R
import tv.nomercy.app.shared.models.MusicList
import tv.nomercy.app.shared.stores.GlobalStores

/**
 * Big play button for album/playlist headers.
 *
 * Self-sufficient component that handles:
 * - Showing play/pause icon based on current playlist state
 * - Toggling playback if this is the current playlist
 * - Starting new playlist if this is a different playlist
 */
@Composable
fun BigPlayButton(
    listData: MusicList?,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    val playlistId = remember(listData?.type, listData?.id) {
        listData?.let { "/music/${it.type}/${it.id}" }
    }

    val isPlaying = musicPlayerStore.isPlaying.collectAsState().value
    val isCurrentPlaylist = musicPlayerStore.isCurrentPlaylist(playlistId)

    val showPause = isPlaying && isCurrentPlaylist

    val enabled = listData?.tracks?.isNotEmpty() == true

    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = backgroundColor.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            .clickable(enabled = enabled) {
                if (listData?.tracks?.isNotEmpty() == true) {
                    if (isCurrentPlaylist) {
                        // Toggle playback for current playlist
                        musicPlayerStore.togglePlayback()
                    } else {
                        // Start new playlist from first track
                        val firstTrack = listData.tracks.first()
                        musicPlayerStore.playTrack(
                            track = firstTrack,
                            tracks = listData.tracks,
                            playlistId = playlistId
                        )
                    }
                }
            }
            .zIndex(10f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = if (showPause) R.drawable.nmpausesolid else R.drawable.nmplaysolid),
            contentDescription = if (showPause) "Pause" else "Play",
            modifier = Modifier.size(36.dp),
            tint = Color.White
        )
    }
}
