package tv.nomercy.app.shared.components.music

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.R
import tv.nomercy.app.shared.components.MoooomIcon
import tv.nomercy.app.shared.components.MoooomIconName
import tv.nomercy.app.shared.components.nMComponents.CoverImage
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.pickPaletteColor

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    val currentSong by musicPlayerStore.currentSong.collectAsState()

    if(currentSong == null) {
        // No song is playing, don't show the mini player
        return
    }

    val queue by musicPlayerStore.queueList.collectAsState()
    val timeState by musicPlayerStore.timeState.collectAsState()
    val backlog by musicPlayerStore.backlog.collectAsState()

    val fullPlaylist = remember(currentSong, queue, backlog) {
        buildList {
            currentSong?.let { add(it) }
            addAll(queue)
        }
    }

    val currentIndex = remember(currentSong, fullPlaylist) {
        currentSong?.let { song ->
            fullPlaylist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        } ?: 0
    }

    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { fullPlaylist.size }
    )

    var lyricsExpanded by remember { mutableStateOf(false) }

    val fallbackColor = MaterialTheme.colorScheme.primary
    val focusColor = remember(currentSong) {
        currentSong?.colorPalette?.cover?.let { palette ->
            pickPaletteColor(palette, 20, 160)
        } ?: fallbackColor
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scrollState = rememberScrollState()

    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.Black,
            dragHandle = null,
            shape = RectangleShape,
            scrimColor = Color.Transparent,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                focusColor.copy(alpha = 0.7f),
                                focusColor.copy(alpha = 0.3f)
                            )
                        ) )
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                val collapsedLyricsHeight = 400.dp
                val expandedLyricsHeight = minHeight - MaterialTheme.typography.titleMedium.fontSize.value.dp - 30.dp

                val lyricsHeight by animateDpAsState(
                    targetValue = if (lyricsExpanded) expandedLyricsHeight else collapsedLyricsHeight,
                    animationSpec = tween(300),
                    label = "lyricsHeight"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(expandedLyricsHeight)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TopRow(
                                onDismiss = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (fullPlaylist.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) { page ->
                                        fullPlaylist.getOrNull(page)?.let {
                                            CoverArtwork(item = it, modifier = Modifier.fillMaxSize())
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TrackRow(item = currentSong)

                            Spacer(modifier = Modifier.height(12.dp))

                            PlayerProgressBar(
                                currentTime = timeState.position,
                                duration = timeState.duration,
                                onSeek = { musicPlayerStore.seekTo(it) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ButtonContainer(
                                activeColor = focusColor,
                                controlRowHeight = 48.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LyricsContainer(
                        isExpanded = lyricsExpanded,
                        height = lyricsHeight,
                        onToggleExpand = {
                            lyricsExpanded = !lyricsExpanded
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(40L)
                                bringIntoViewRequester.bringIntoView()
                            }
                        },
                        activeColor = focusColor,
                        bringIntoViewRequester = bringIntoViewRequester,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .bringIntoViewRequester(bringIntoViewRequester)
                    )
                }
            }
        }
    }
}


@Composable
private fun CoverArtwork(
    item: PlaylistItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        CoverImage(
            cover = item.cover,
            name = item.name,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun TopRow(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val currentSong by musicPlayerStore.currentSong.collectAsState()

    val playlistTitle = remember(currentSong) {
        currentSong?.albumTrack?.firstOrNull()?.name
    }

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp)
        ) {
            MoooomIcon(
                icon = MoooomIconName.ChevronDown,
                contentDescription = "Close",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (playlistTitle != null) {
                Text(
                    text = "Now playing",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = playlistTitle,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(48.dp)
            ) {
                MoooomIcon(
                    icon = MoooomIconName.MenuDotsVertical,
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color.DarkGray)
            ) {
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = Color.White) },
                    onClick = {
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Share", color = Color.White) },
                    onClick = {
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Go to Album", color = Color.White) },
                    onClick = {
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TrackRow(
    item: PlaylistItem?,
    modifier: Modifier = Modifier
) {
    if (item == null) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (item.artistTrack.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.artistTrack.forEachIndexed { index, artist ->
                        Text(
                            text = artist.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.clickable {
                            }
                        )

                        if (index < item.artistTrack.size - 1) {
                            Text(
                                text = ", ",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        MediaLikeButton(
            favorite = item.favorite,
            color = Color(0xFFFF6B9D),
        )
    }
}

@Composable
private fun ButtonContainer(
     modifier: Modifier = Modifier,
     activeColor: Color = Color.White,
     controlRowHeight: Dp = 40.dp
 ) {
     Column(
         modifier = modifier.fillMaxWidth(),
         verticalArrangement = Arrangement.spacedBy(0.dp)
     ) {
         Row(
             modifier = Modifier
                 .fillMaxWidth(),
             horizontalArrangement = Arrangement.SpaceBetween,
             verticalAlignment = Alignment.CenterVertically
         ) {
             ShuffleButton(activeColor = activeColor)
             PreviousButton()
             StyledPlaybackButton(
                 modifier = Modifier.size(56.dp),
                 backgroundColor = activeColor
             )
             NextButton()
             RepeatButton(activeColor = activeColor)
         }

         Spacer(modifier = Modifier.height(12.dp))

         Row(
             modifier = Modifier
                 .fillMaxWidth()
                 .height(controlRowHeight),
             horizontalArrangement = Arrangement.SpaceBetween,
             verticalAlignment = Alignment.CenterVertically
         ) {
             DeviceButton(activeColor = activeColor)

             Row(
                 horizontalArrangement = Arrangement.spacedBy(6.dp),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 StopButton()
                 QueueButton(activeColor = activeColor)
             }
         }
     }
 }

@Composable
private fun StyledPlaybackButton(
     modifier: Modifier = Modifier,
     backgroundColor: Color = Color.White
 ) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val isPlaying = musicPlayerStore.isPlaying.collectAsState().value

    Box(
        modifier = modifier
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
            .clickable {
                musicPlayerStore.togglePlayback()
            }
            .zIndex(10f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = if (isPlaying) R.drawable.nmpausesolid else R.drawable.nmplaysolid),
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(36.dp),
            tint = Color.White
        )
    }
}
