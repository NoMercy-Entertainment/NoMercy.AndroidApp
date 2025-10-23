package tv.nomercy.app.views.music.list.tv

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tv.nomercy.app.components.CoverImage
import tv.nomercy.app.components.ShimmerTrackRow
import tv.nomercy.app.components.music.BigPlayButton
import tv.nomercy.app.components.music.MediaLikeButton
import tv.nomercy.app.components.music.TrackRow
import tv.nomercy.app.shared.models.MusicList
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.views.music.list.shared.ListViewModel
import tv.nomercy.app.views.music.list.shared.ListViewModelFactory
import java.util.UUID


@Composable
fun ListScreen(
    type: String,
    id: String,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
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

    val fallbackColor = MaterialTheme.colorScheme.primary
    val palette = listData?.colorPalette?.cover ?: listData?.colorPalette?.image
    val backgroundColor = remember(palette) {
        pickPaletteColor(palette, 80, fallbackColor = fallbackColor)
    }
    val key = remember { UUID.randomUUID() }

    DisposableEffect(backgroundColor) {
        themeOverrideManager.add(key, backgroundColor)
        onDispose { themeOverrideManager.remove(key) }
    }

    val scrollState = rememberLazyListState()

    val lastSelectedIndex = rememberSaveable { mutableIntStateOf(0) }
    val focusRequesters = remember(listData?.tracks) {
        listData?.tracks?.map { FocusRequester() } ?: emptyList()
    }
    LaunchedEffect(listData?.tracks, lastSelectedIndex.intValue) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo }
            .filter { it.isNotEmpty() }
            .first()
        scrollState.animateScrollToItem(lastSelectedIndex.intValue)
        delay(100)
        focusRequesters.getOrNull(lastSelectedIndex.intValue)?.requestFocus()
    }

    val playButtonFocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.4f),
                        Color.Black.copy(alpha = 0.4f)
                    )
                )
            )
            .padding(horizontal = 32.dp)
            .padding(top = 72.dp, bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // LEFT COLUMN: Cover, Title, Type, Play Button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 24.dp, bottom = 52.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection(listData,
                    modifier = Modifier
                        .weight(4f))
                BigHeaderText(listData,
                    modifier = Modifier
                        .weight(1f))
                ControlHeader(listData, backgroundColor,
                    modifier = Modifier
                        .weight(1f),
                    playButtonFocusRequester = playButtonFocusRequester,
                    focusRequesters = focusRequesters,
                    lastSelectedIndex = lastSelectedIndex
                )
            }

            // RIGHT COLUMN: Track List (non-scrolling)
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(top = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SortHeader(Color.Transparent)

                if (listData == null) {
                    repeat(8) {
                        ShimmerTrackRow(
                            modifier = Modifier.testTag("shimmer-track-$it")
                        )
                    }
                } else {

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        itemsIndexed(listData?.tracks ?: emptyList()) { index, track ->
                            TrackRow(
                                data = track,
                                index = index,
                                onClick = {
                                    val currentSong = musicPlayerStore.currentSong.value
                                    if (currentSong?.id == track.id) {
                                        musicPlayerStore.togglePlayback()
                                    } else {
                                        val playlistId =
                                            "/music/${listData!!.type}/${listData!!.id}"
                                        musicPlayerStore.playTrack(
                                            track,
                                            listData!!.tracks,
                                            playlistId
                                        )
                                    }
                                },
                                onContextMenu = { /* Handle context menu */ },
                                navController = navController,
                                focusRequesters = focusRequesters,
                                playButtonFocusRequester = playButtonFocusRequester,
                                lazyListState = scrollState,
                                modifier = Modifier
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            lastSelectedIndex.intValue = index

                                            scope.launch {
                                                delay(10)
                                                scrollState.animateScrollToItem(index)
                                            }
                                        }
                                    }
                                    .testTag("track-${listData!!.id}"),
                                isAlbumRoute = listData!!.type == "albums",
                                isArtistRoute = listData!!.type == "artists",
                                backgroundColor = backgroundColor
                            )
                        }
                    }
                }
            }
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
            .padding(end = 60.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        if (listData != null) {
            CoverImage(
                cover = listData.cover,
                name = listData.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectFromType(AspectRatio.Cover)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 24.dp),
        verticalArrangement= Arrangement.spacedBy(8.dp),
    ) {

        Text(
            text = listData?.name ?: "",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

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
fun ControlHeader(
    listData: MusicList?,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    playButtonFocusRequester: FocusRequester,
    focusRequesters: List<FocusRequester>,
    lastSelectedIndex: MutableIntState
) {
    Row(
        modifier = modifier
            .padding(end = 52.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {

            MediaLikeButton(
                favorite = listData?.favorite,
                color = backgroundColor
            )
        }

        BigPlayButton(
            listData = listData,
            backgroundColor = backgroundColor,
            modifier = Modifier
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action != KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        focusRequesters.getOrNull(lastSelectedIndex.intValue)?.requestFocus()
                        return@onPreviewKeyEvent true
                    }
                    false
                }
                .focusRequester(playButtonFocusRequester)
        )
    }
}


enum class SortDirection { ASC, DESC }

@Composable
fun SortHeader(backgroundColor: Color) {
    // Local sort state (key + direction) for the header
    val selectedKey = remember { mutableStateOf("#") }
    val direction = remember { mutableStateOf(SortDirection.ASC) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors =
                            if (backgroundColor == Color.Transparent) listOf(Color.Transparent, Color.Transparent)
                            else listOf(backgroundColor.copy(alpha = 0.6f), backgroundColor.copy(alpha = 0.6f))
                    )
                )
                .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SortButton(
                text = "#",
                backgroundColor = backgroundColor,
                modifier = Modifier
                    .width(32.dp),
                direction = if (selectedKey.value == "#") direction.value else null,
                onClick = {
                    if (selectedKey.value == "#") {
                        direction.value = if (direction.value == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
                    } else {
                        selectedKey.value = "#"
                        direction.value = SortDirection.ASC
                    }
                }
            )
            SortButton(
                text = "Title",
                backgroundColor = backgroundColor,
                modifier = Modifier
                    .width(56.dp),
                direction = if (selectedKey.value == "Title") direction.value else null,
                onClick = {
                    if (selectedKey.value == "Title") {
                        direction.value = if (direction.value == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
                    } else {
                        selectedKey.value = "Title"
                        direction.value = SortDirection.ASC
                    }
                }
            )
        }

        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun SortButton(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    direction: SortDirection? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors =
                        if (backgroundColor == Color.Transparent) listOf(Color.Transparent, Color.Transparent)
                        else listOf(backgroundColor.copy(alpha = 0.6f), backgroundColor.copy(alpha = 0.6f))
                )
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .focusable()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (direction != null) {
                Text(
                    text = if (direction == SortDirection.ASC) "▲" else "▼",
                    modifier = Modifier.padding(start = 6.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
