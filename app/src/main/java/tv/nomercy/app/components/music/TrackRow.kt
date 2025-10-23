package tv.nomercy.app.components.music

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.R
import tv.nomercy.app.components.DropdownMenuButton
import tv.nomercy.app.components.Marquee
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.dpadFocus


@Composable
fun TrackRow(
    data: PlaylistItem,
    index: Int,
    onClick: () -> Unit,
    onContextMenu: (Offset) -> Unit,
    isAlbumRoute: Boolean,
    isArtistRoute: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    backgroundColor: Color,
    focusRequesters: List<FocusRequester> = emptyList(),
    playButtonFocusRequester : FocusRequester? = null,
    lazyListState: LazyListState? = null,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    val currentSong = musicPlayerStore.currentSong.collectAsState().value
    val isPlaying = musicPlayerStore.isPlaying.collectAsState().value

    val isCurrentTrack = currentSong?.id == data.id

    val favoriteFocusRequester = remember { FocusRequester() }
    val menuFocusRequester = remember { FocusRequester() }

    val currentlyFocusedIndex = remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .then(if (focusRequesters.isEmpty()) Modifier else Modifier
                .focusRequester(focusRequesters[index]))
            .dpadFocus(
                onLeft = {
                    if (currentlyFocusedIndex.intValue != 0) return@dpadFocus false
                    playButtonFocusRequester?.requestFocus()
                    true
                },
                onRight = {
                    if (currentlyFocusedIndex.intValue > 0) return@dpadFocus false
                    favoriteFocusRequester.requestFocus()
                    currentlyFocusedIndex.intValue = 1
                    scope.launch {
                        delay(10)
                        lazyListState?.animateScrollToItem(index)
                    }
                    true
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { onContextMenu(Offset.Zero) }
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
                    Text(text = "${index + 1}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Icon(
                        painter = painterResource(id = if(isPlaying) R.drawable.nmpausesolid else R.drawable.nmplaysolid),
                        contentDescription = if(isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Cover + Title + Artist/Album links
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
                            if (!isArtistRoute) {
                                TrackLinksArtists(data.artistTrack, navController)
                            }
                            if (!isAlbumRoute) {
                                TrackLinksAlbums(data.albumTrack, navController)
                            }
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
                        .dpadFocus(
                            onLeft = {
                                focusRequesters[index].requestFocus()
                                currentlyFocusedIndex.intValue = 0

                                scope.launch {
                                    delay(10)
                                    lazyListState?.animateScrollToItem(index)
                                }
                                true
                            },
                            onRight = {
                                menuFocusRequester.requestFocus()
                                currentlyFocusedIndex.intValue = 2

                                scope.launch {
                                    delay(10)
                                    lazyListState?.animateScrollToItem(index)
                                }
                                true
                            }
                        )
                ) {
                    MediaLikeButton(favorite = data.favorite, color = backgroundColor)
                }

                Box(
                    modifier = Modifier
                        .focusRequester(menuFocusRequester)
                        .dpadFocus(
                            onLeft = {
                                favoriteFocusRequester.requestFocus()
                                currentlyFocusedIndex.intValue = 1
                                scope.launch {
                                    delay(10)
                                    lazyListState?.animateScrollToItem(index)
                                }
                                true
                            }
                        )
                ) {
                    DropdownMenuButton {
                        Text("Hellooo")
                    }
                }
            }
        }
    }
}
