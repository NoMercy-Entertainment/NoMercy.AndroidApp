package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.nomercy.app.R
import tv.nomercy.app.components.images.CoverImage
import tv.nomercy.app.components.DropdownMenuButton
import tv.nomercy.app.components.Marquee
import tv.nomercy.app.components.music.MediaLikeButton
import tv.nomercy.app.components.music.TrackLinksArtists
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMTrackRowWrapper
import tv.nomercy.app.shared.stores.GlobalStores

@Composable
fun NMTrackRow(
    component: Component,
    modifier: Modifier,
    navController: NavHostController,
){
    val wrapper = component.props as? NMTrackRowWrapper ?: return
    val data = wrapper.data ?: return

    val context = LocalContext.current

    val serverConfigStore = GlobalStores.getServerConfigStore(context)
    serverConfigStore.currentServer.collectAsState()

    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    val currentSong = musicPlayerStore.currentSong.collectAsState().value
    val isPlaying = musicPlayerStore.isPlaying.collectAsState().value

    val isCurrentTrack = currentSong?.id == data.id

    val favoriteFocusRequester = remember { FocusRequester() }
    val menuFocusRequester = remember { FocusRequester() }

    val fallbackColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {

                },
                onLongClick = {

                }
            )
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Index / Play Icon
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isCurrentTrack) {
                Text(text = "", style = MaterialTheme.typography.bodyMedium)
            } else {
                Icon(
                    painter = painterResource(id = if(isPlaying) R.drawable.nmpausesolid else R.drawable.nmplaysolid),
                    contentDescription = if(isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CoverImage(
                cover = data.cover,
                name = data.name,
                modifier = Modifier
                    .size(40.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = data.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Marquee {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TrackLinksArtists(data.artistTrack, navController)
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .focusRequester(favoriteFocusRequester)
            ) {
                MediaLikeButton(favorite = data.favorite, color = fallbackColor)
            }

            Box(
                modifier = Modifier
                    .focusRequester(menuFocusRequester)
            ) {
                DropdownMenuButton {
                    Text("Hellooo")
                }
            }
        }
    }

}