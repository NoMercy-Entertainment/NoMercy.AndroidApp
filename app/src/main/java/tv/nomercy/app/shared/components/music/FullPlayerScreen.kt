package tv.nomercy.app.shared.components.music

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import tv.nomercy.app.R
import tv.nomercy.app.shared.components.MoooomIcon
import tv.nomercy.app.shared.components.MoooomIconName
import tv.nomercy.app.shared.components.nMComponents.CoverImage
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.pickPaletteColor
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    val currentSong by musicPlayerStore.currentSong.collectAsState()
    val queue by musicPlayerStore.queueList.collectAsState()
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
    val lyricsHeight by animateDpAsState(
        targetValue = if (lyricsExpanded) 800.dp else 400.dp,
        animationSpec = tween(300),
        label = "lyricsHeight"
    )

    val focusColor = remember(currentSong) {
        currentSong?.colorPalette?.cover?.let { palette ->
            pickPaletteColor(palette, 20, 160)
        } ?: Color(0xFF1A1A1A)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentSong, fullPlaylist) {
        if (currentSong != null && fullPlaylist.isNotEmpty()) {
            val index = fullPlaylist.indexOfFirst { it.id == currentSong?.id }
            if (index >= 0 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                val item = fullPlaylist.getOrNull(page)
                if (item != null && item.id != currentSong?.id) {
                    musicPlayerStore.playTrack(item, fullPlaylist, null)
                }
            }
    }

    LaunchedEffect(currentSong?.id) {
        if (currentSong != null && fullPlaylist.isNotEmpty()) {
            val index = fullPlaylist.indexOfFirst { it.id == currentSong?.id }
            if (index >= 0 && index != pagerState.currentPage) {
                pagerState.scrollToPage(index)
            }
        }
    }

    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.Black,
            dragHandle = null,
            shape = RectangleShape,
            scrimColor = Color.Transparent,
        ) {
            val scrollState = rememberScrollState()
            var lyricsContainerOffset by remember { mutableStateOf(0) }
            val coroutineScope = rememberCoroutineScope()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                focusColor.copy(alpha = 0.7f),
                                focusColor.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                TopRow(
                    onDismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                Spacer(modifier = Modifier.weight(0.1f))

                if (fullPlaylist.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) { page ->
                            val item = fullPlaylist.getOrNull(page)
                            item?.let {
                                CoverArtwork(item = it, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TrackRow(
                    item = currentSong,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                PlayerProgressBar(
                    currentTime = musicPlayerStore.timeState.collectAsState().value.position,
                    duration = musicPlayerStore.timeState.collectAsState().value.duration,
                    onSeek = { musicPlayerStore.seekTo(it) },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ButtonContainer(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    activeColor = focusColor
                )

                LyricsContainer(
                    isExpanded = lyricsExpanded,
                    height = lyricsHeight,
                    onToggleExpand = { lyricsExpanded = !lyricsExpanded },
                    activeColor = focusColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .onGloballyPositioned { coordinates ->
                            lyricsContainerOffset = coordinates.positionInParent().y.toInt()
                        }
                )
            }
            LaunchedEffect(lyricsExpanded) {
                if (lyricsExpanded) {
                    coroutineScope.launch {
                        // Scroll so the lyrics container is at least visible
                        scrollState.animateScrollTo(lyricsContainerOffset)
                    }
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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceButton(activeColor = activeColor)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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

@Composable
private fun LyricsContainer(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    height: Dp,
    onToggleExpand: () -> Unit,
    activeColor: Color = Color.White,
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val currentSong by musicPlayerStore.currentSong.collectAsState()
    val timeState by musicPlayerStore.timeState.collectAsState()
    val userStore = GlobalStores.getAuthStore(context)
    val serverStore = GlobalStores.getServerConfigStore(context)
    val accessToken = userStore.accessToken
    val serverUrl = serverStore.currentServer.collectAsState().value?.serverApiUrl

    suspend fun fetchLyrics(): List<LyricLine>? {
        val songId = currentSong?.id ?: return null
        val url = "$serverUrl/music/tracks/$songId/lyrics"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body.string()
                    val arr = JSONArray(body)
                    List(arr.length()) { i ->
                        val obj = arr.getJSONObject(i)
                        LyricLine(
                            time = obj.optDouble("time", 0.0).toFloat(),
                            text = obj.optString("text", "")
                        )
                    }
                } else null
            }
        }
    }

    Column(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(activeColor.copy(alpha = 0.8f))
            .zIndex(2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.lyrics),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            MoooomIcon(
                icon = if (isExpanded) MoooomIconName.ArrowCollapse else MoooomIconName.ArrowExpand,
                contentDescription = if (isExpanded) stringResource(id = R.string.collapse) else stringResource(id = R.string.expand),
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .clickable{
                        onToggleExpand()
                    }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .weight(1f)
        ) {
            LyricsView(
                lyrics = currentSong?.lyrics?.map { LyricLine(
                    it.time.total.toFloat(),
                    it.text
                ) },
                currentTime = timeState.position,
                fetchLyrics = { fetchLyrics() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FlatSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    barHeight: Dp = 8.dp,
    cornerRadius: Dp = 50.dp
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(value) }

    val coercedValue = dragPosition.coerceIn(valueRange.start, valueRange.endInclusive)

    Box(
        modifier = modifier
            .height(barHeight * 2)
            .pointerInput(valueRange, isDragging) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        val width = size.width
                        val percent = (change.position.x / width).coerceIn(0f, 1f)
                        dragPosition = valueRange.start + percent * (valueRange.endInclusive - valueRange.start)
                        onValueChange(dragPosition)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished()
                    },
                    onDragCancel = {
                        isDragging = false
                        onValueChangeFinished()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width
            val barY = size.height / 2
            val progress = ((coercedValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            drawRoundRect(
                color = backgroundColor,
                topLeft = Offset(0f, barY - barHeight.toPx() / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, barY - barHeight.toPx() / 2),
                size = androidx.compose.ui.geometry.Size(barWidth * progress, barHeight.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }
    }
}

@Composable
private fun PlayerProgressBar(
    currentTime: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var timeChoice by rememberSaveable { mutableStateOf("remaining") }
    val displayCurrent = humanTime(currentTime)
    val displayEnd = if (timeChoice == "duration") humanTime(duration) else humanTime(duration - currentTime)

    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember(currentTime, duration) { mutableFloatStateOf(currentTime.toFloat()) }
    LaunchedEffect(currentTime, isDragging) {
        if (!isDragging) {
            sliderPosition = currentTime.toFloat()
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = displayCurrent,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.widthIn(min = 48.dp),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start
        )
        FlatSeekBar(
            value = sliderPosition,
            onValueChange = {
                isDragging = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek(sliderPosition.roundToInt().toLong())
            },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            barColor = Color.White,
            backgroundColor = Color.White.copy(alpha = 0.2f),
            barHeight = 8.dp,
            cornerRadius = 50.dp
        )
        Text(
            text = displayEnd,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .widthIn(min = 48.dp)
                .clickable { timeChoice = if (timeChoice == "duration") "remaining" else "duration" },
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End
        )
    }
}

private fun humanTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
