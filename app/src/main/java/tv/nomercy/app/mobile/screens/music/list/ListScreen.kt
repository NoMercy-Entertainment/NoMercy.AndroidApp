package tv.nomercy.app.mobile.screens.music.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tv.nomercy.app.shared.components.ShimmerBox
import tv.nomercy.app.shared.components.ShimmerTrackRow
import tv.nomercy.app.shared.components.music.BigPlayButton
import tv.nomercy.app.shared.components.music.MediaLikeButton
import tv.nomercy.app.shared.components.music.ShareButton
import tv.nomercy.app.shared.components.music.TrackRow
import tv.nomercy.app.shared.components.nMComponents.CoverImage
import tv.nomercy.app.shared.models.MusicList
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.pickPaletteColor
import java.util.UUID


@Composable
fun ListScreen(
    type: String,
    id: String,
    navController: NavHostController
) {
    val themeOverrideManager = LocalThemeOverrideManager.current
    val context = LocalContext.current
    val viewModel: ListViewModel = viewModel(
        factory = ListViewModelFactory(
            listStore = GlobalStores.getListStore(context),
        )
    )

    val listData by viewModel.list.collectAsState()

    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    LaunchedEffect(type, id) {
        viewModel.selectList(type, id)
    }

    val scrollState = rememberLazyListState()
    val palette = listData?.colorPalette?.cover ?: listData?.colorPalette?.image
    val backgroundColor = remember(palette) { pickPaletteColor(palette, 80) }
    val key = remember { UUID.randomUUID() }

    DisposableEffect(backgroundColor) {
        themeOverrideManager.add(key, backgroundColor)

        onDispose {
            themeOverrideManager.remove(key)
        }
    }

    Box(
        modifier = Modifier
            .padding(0.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.4f),
                        Color.Black.copy(alpha = 0.4f)
                    )
                )
            )
            .fillMaxSize()
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .padding(0.dp)
                .fillMaxSize()
        ) {
            item {
                HeaderSection(
                    listData = listData,
                    modifier = Modifier
                )
            }

            item {
                BigHeaderText(
                    listData = listData,
                    modifier = Modifier
                )
            }

            item {
                ControlHeader(listData, backgroundColor)
            }

            item {
                SortHeader(Color.Transparent)
            }

            if (listData == null) {
                items(8) {
                    ShimmerTrackRow(
                        modifier = Modifier.testTag("shimmer-track-$it")
                    )
                }
            } else {
                items(listData?.tracks ?: emptyList()) { track ->
                    TrackRow(
                        data = track,
                        index = listData?.tracks?.indexOf(track) ?: 0,
                        onClick = {
                            val currentSong = musicPlayerStore.currentSong.value
                            if (currentSong?.id == track.id) {
                                musicPlayerStore.togglePlayback()
                            } else {
                                val playlistId = "/music/${listData?.type}/${listData?.id}"
                                musicPlayerStore.playTrack(track, listData?.tracks, playlistId)
                            }
                        },
                        onContextMenu = { offset ->
                            // Handle context menu
                        },
                        navController = navController,
                        modifier = Modifier
                            .testTag("track-${listData?.id}"),
                        isAlbumRoute = listData?.type == "albums",
                        isArtistRoute = listData?.type == "artists",
                        backgroundColor = backgroundColor
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            ScrollSync(
                scrollState = scrollState,
                listData = listData,
                backgroundColor = backgroundColor
            )
        }
    }
}

@Composable
fun HeaderSection(
    listData: MusicList?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 60.dp, vertical = 40.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (listData != null) {
            CoverImage(
                cover = listData.cover,
                name = listData.name,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectFromType(AspectRatio.Cover)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
            )
        } else {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectFromType(AspectRatio.Cover)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun BigHeaderText(
    listData: MusicList?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp),
        verticalArrangement= Arrangement.spacedBy(16.dp),
    ) {

        if (listData != null) {
            Text(
                text = listData.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        else {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val artist = listData?.artists?.firstOrNull()

            if(listData?.type == "albums" && artist != null && artist.cover != null) {
                CoverImage(
                    cover = artist.cover,
                    name = artist.name,
                    modifier = Modifier
                        .width(44.dp)
                        .height(44.dp)
                        .aspectFromType(AspectRatio.Cover)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                        .clip(CircleShape),
                )
            }

            Text(
                text = artist?.name ?: "",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(listData?.type?.replaceFirstChar { it.uppercase() }?.removeSuffix("s") ?: "", fontSize = 16.sp)
            Text("•")
            Text("${listData?.year ?: listData?.tracks?.firstOrNull()?.albumTrack?.firstOrNull()?.year ?: ""}", fontSize = 14.sp)
        }

    }
}

@Composable
fun ControlHeader(listData: MusicList?, backgroundColor: Color) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            MediaLikeButton(
                favorite = listData?.favorite,
                color = backgroundColor
            )

            ShareButton(
                link = listData?.link ?: "",
                onClick = {
                    // Handle share action
                }
            )
        }

        BigPlayButton(
            listData = listData,
            backgroundColor = backgroundColor
        )
    }
}


@Composable
fun ScrollSync(
    scrollState: LazyListState,
    listData: MusicList?,
    backgroundColor: Color
) {
    val showTitle by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0
        }
    }

    val showSortHeader by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = showTitle,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    if (backgroundColor == Color.Transparent) listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                    else listOf(
                                        backgroundColor.copy(alpha = 0.5f),
                                        backgroundColor.copy(alpha = 0.5f),
                                        backgroundColor.copy(alpha = 0.3f)
                                    )
                            )
                        )
                        .padding(vertical = 18.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = listData?.name ?: "Unknown",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                    )
                }
            }
        }

//        AnimatedVisibility(
//            visible = showBigPlayButton,
//            enter = androidx.compose.animation.fadeIn(),
//            exit = androidx.compose.animation.fadeOut()
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                BigPlayButton(
//                    onClick = {
//                        // Handle play action
//                    },
//                    backgroundColor = backgroundColor,
//                    enabled = (listData?.tracks?.isNotEmpty() == true)
//                )
//            }
//        }

        AnimatedVisibility(
            visible = showSortHeader,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxWidth()
            ) {
                SortHeader(backgroundColor)
            }
        }
    }
}

@Composable
fun SortHeader(backgroundColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors =
                        if (backgroundColor == Color.Transparent) listOf(Color.Transparent,Color.Transparent)
                        else listOf(backgroundColor.copy(alpha = 0.6f), backgroundColor.copy(alpha = 0.6f))
                )
            )
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "#",
            modifier = Modifier.width(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text("Title")
    }
}
