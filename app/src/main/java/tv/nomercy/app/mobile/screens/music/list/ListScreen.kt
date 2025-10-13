package tv.nomercy.app.mobile.screens.music.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tv.nomercy.app.R
import tv.nomercy.app.shared.components.nMComponents.CoverImage
import tv.nomercy.app.shared.components.ShimmerBox
import tv.nomercy.app.shared.models.Album
import tv.nomercy.app.shared.models.Artist
import tv.nomercy.app.shared.models.MusicList
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.models.Track
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
    val serverConfigStore = GlobalStores.getServerConfigStore(context)
    val currentServer = serverConfigStore.currentServer.collectAsState()

    val listData by viewModel.list.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Select the card set when the screen appears
    LaunchedEffect(type, id) {
        viewModel.selectList(type, id)
    }

    val scrollState = rememberLazyListState()
    val palette = listData?.colorPalette?.cover ?: listData?.colorPalette?.image
    val backgroundColor = remember(palette) { pickPaletteColor(palette) }
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
            .fillMaxSize()
    ) {
        // Main content
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .padding(0.dp)
                .fillMaxSize()
        ) {
            item {
                HeaderSection(
                    listData = listData,
                    currentServer = currentServer,
                    modifier = Modifier
                        .background(backgroundColor)
                )
            }

            item {
                ControlHeader(listData, backgroundColor)
            }

            item {
                SortHeader(listData)
            }

            if (listData == null) {
                // Show shimmer loading states for tracks
                items(8) {
                    ShimmerTrackRow(
                        modifier = Modifier.testTag("shimmer-track-$it")
                    )
                }
            } else {
                val currentList = listData!!
                val tracks = currentList.tracks
                val listType = currentList.type
                val listId = currentList.id
                items(tracks) { track ->
                    TrackRow(
                        track, tracks.indexOf(track),
                        isPlaying = false, // Replace with actual playing state
                        currentSongId = null, // Replace with actual current song ID
                        onClick = {
                            // Handle track click
                        },
                        onContextMenu = { offset ->
                            // Handle context menu
                        },
                        currentServer = currentServer,
                        navController = navController,
                        modifier = Modifier
                            .testTag("track-$listId"),
                        isAlbumRoute = listType == "albums",
                        isArtistRoute = listType == "artists",
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
            ScrollSync(scrollState = scrollState, listData = listData, backgroundColor = backgroundColor)
        }
    }
    
}

@Composable
fun HeaderSection(
    listData: MusicList?,
    currentServer: State<Server?>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(0.dp)
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
                    .padding(start = 52.dp, end = 52.dp, top = 52.dp, bottom = 40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                currentServer = currentServer
            )
        } else {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectFromType(AspectRatio.Cover)
                    .padding(start = 52.dp, end = 52.dp, top = 52.dp, bottom = 40.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp, bottom = 32.dp),
            verticalArrangement= Arrangement.SpaceBetween
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
            } else {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

//            Text("Year: ${listData?.year ?: listData?.tracks?.firstOrNull()?.albumTrack?.firstOrNull()?.year ?: "Unknown"}")
        }
    }
}

@Composable
fun ControlHeader(listData: MusicList?, backgroundColor: Color) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MediaLikeButton(
                favorite = listData?.favorite,
                color = backgroundColor
            )
            ShareButton(link = listData?.link ?: "")
        }

        BigPlayButton(
            onClick = {
                // Handle play action
            },
            backgroundColor = backgroundColor,
            enabled = (listData?.tracks?.isNotEmpty() == true)
        )
    }
}

@Composable
fun BigPlayButton(onClick: () -> Unit, enabled: Boolean, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.nmplaysolid),
            contentDescription = "Play",
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ShareButton(link: String) {
    Icon(
        painter = painterResource(id = R.drawable.sharesquare),
        contentDescription = "Share",
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable {
                // Handle share action
            },
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun TrackRow(
    data: Track,
    index: Int,
    isPlaying: Boolean,
    currentSongId: String?,
    onClick: () -> Unit,
    onContextMenu: (Offset) -> Unit,
    currentServer: State<Server?>,
    isAlbumRoute: Boolean,
    isArtistRoute: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    backgroundColor: Color,
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onContextMenu(Offset.Zero) } // Replace with actual offset if needed
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Index / Play Icon
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentSongId != data.id) {
                Text(text = "${index + 1}", style = MaterialTheme.typography.bodyMedium)
            } else {
                Icon(
                    painter = painterResource(id = if(isPlaying) R.drawable.nmpausesolid else R.drawable.nmplaysolid),
                    contentDescription = "Pause",
                    modifier = Modifier.size(24 .dp),
                    tint = backgroundColor
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
//            CoverImage(
//                cover = data.cover,
//                name = data.name,
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(RoundedCornerShape(8.dp)),
//                currentServer = currentServer
//            )

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

                MarqueeText {
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

        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaLikeButton(favorite = data.favorite, color = backgroundColor)

            DropdownMenuButton {
                Text("Hellooo") // Replace with actual menu items
            }
        }
    }
}

@Composable
fun DropdownMenuButton(content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            painter = painterResource(id = R.drawable.menudotsvertical),
            contentDescription = "More",
            modifier = Modifier
                .size(32.dp)
                .clickable { expanded = true },
            tint = MaterialTheme.colorScheme.onSurface
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            content()
        }
    }
}

@Composable
fun MediaLikeButton(favorite: Boolean?, color: Color) {
    Icon(
        painter = painterResource(id = if(favorite == true) R.drawable.heartfilled else R.drawable.heart),
        contentDescription = "Play",
        modifier = Modifier.size(32.dp),
        tint = if(favorite == true) color else MaterialTheme.colorScheme.onSurface
    )
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
        AnimatedVisibility(showTitle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(vertical = 16.dp, horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = listData?.name ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                )
            }
        }

        if (showSortHeader) {
            SortHeader(listData)
        }
    }
}

@Composable
fun SortHeader(listData: MusicList?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text("#")
        Text("Title")
    }
}


@Composable
fun TrackLinksAlbums(albums: List<Album>, navController: NavHostController) {
    albums.forEach { link ->
        Text(
            text = link.name + if (albums.size > 1) "," else "",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .clickable {
                    navController.navigate(link.link)
                }
                .padding(end = 1.dp)
        )
    }
}

@Composable
fun TrackLinksArtists(artists: List<Artist>, navController: NavHostController) {
    artists.forEach { link ->
        Text(
            text = link.name + if (artists.size > 1) "," else "",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .clickable {
                    navController.navigate(link.link)
                }
                .padding(end = 1.dp)
        )
    }
}

@Composable
fun MarqueeText(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState()),
        content = content
    )
}

@Composable
fun ShimmerTrackRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Index
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(20.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
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
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
