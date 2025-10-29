package tv.nomercy.app.components.music

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import tv.nomercy.app.R
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.isTv

// Data class for a lyric line
data class LyricStyle(
    val fontSize: TextUnit,
    val lineHeight: TextUnit,
    val iconSize: Dp,
    val minHeight: Dp
)

data class LyricLine(
    val time: Float,
    val text: String
)

@Composable
fun rememberLyricStyle(): LyricStyle {
    return if (isTv()) {
        LyricStyle(
            fontSize = 40.sp,
            lineHeight = 48.sp,
            iconSize = 40.dp,
            minHeight = 96.dp
        )
    } else {
        LyricStyle(
            fontSize = 20.sp,
            lineHeight = 22.sp,
            iconSize = 20.dp,
            minHeight = 46.dp
        )
    }
}

@Composable
fun LyricItem(
    lyric: LyricLine,
    index: Int,
    currentIndex: Int,
    style: LyricStyle,
    modifier: Modifier = Modifier
) {
    val targetColor = if (index <= currentIndex) Color.White else Color.Black
    val targetAlpha = if (index < currentIndex) 0.5f else 1f

    val animatedColor by animateColorAsState(targetColor, label = "lyricColor")
    val animatedAlpha by animateFloatAsState(targetAlpha, label = "lyricAlpha")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = style.minHeight)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (lyric.text.isNotEmpty()) {
            Text(
                text = lyric.text,
                color = animatedColor.copy(alpha = animatedAlpha),
                fontSize = style.fontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = style.lineHeight,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 2
            )
        } else {
            MoooomIcon(
                icon = MoooomIconName.NoteDouble,
                modifier = Modifier.size(style.iconSize),
                tint = animatedColor.copy(alpha = animatedAlpha),
                contentDescription = "Music Note"
            )
        }
    }
}

@Composable
fun LyricsView(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val currentSong by musicPlayerStore.currentSong.collectAsState()
    val timeState by musicPlayerStore.timeState.collectAsState()
    val accessToken by GlobalStores.getAuthStore(context).accessToken.collectAsState()
    val serverUrl = GlobalStores.getServerConfigStore(context).currentServer.collectAsState().value?.serverApiUrl

    val lyrics = currentSong?.lyrics?.map { LyricLine(it.time.total.toFloat(), it.text) }
    var loadedLyrics by remember { mutableStateOf(lyrics) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var viewportHeight by remember { mutableIntStateOf(0) }

    val isTv = isTv()

    val currentTimeSec = remember(timeState.position) { timeState.position / 1000f }

    val currentIndex by remember(loadedLyrics, currentTimeSec) {
        derivedStateOf {
            loadedLyrics?.let { lines ->
                val adjustedTime = currentTimeSec + 0.5f
                val idx = (lines.indexOfFirst { it.time >= adjustedTime }.takeIf { it >= 0 } ?: lines.size) - 1
                if (idx == -1 && adjustedTime > 1f) -1 else idx
            } ?: -1
        }
    }

    var lastScrolledIndex by remember { mutableIntStateOf(-2) }

    LaunchedEffect(currentSong?.id) {
        loadedLyrics = if (loadedLyrics == null) {
            fetchLyrics(serverUrl ?: "", accessToken ?: "", currentSong)
        } else lyrics

        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(currentIndex, viewportHeight) {
        if (isExpanded && currentIndex >= 0 && currentIndex != lastScrolledIndex && viewportHeight > 0) {
            lastScrolledIndex = currentIndex
            val offset = if (isTv) -190 else -(viewportHeight / 3)
            coroutineScope.launch {
                scrollToItemWithOffsetAndDuration(
                    listState = listState,
                    targetIndex = currentIndex,
                    offsetFromTop = offset,
                    durationMillis = if (isTv) 300 else 500
                )
            }
        }
    }


    if (isExpanded) {
        val style = rememberLyricStyle()

        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { viewportHeight = it.size.height },
            contentAlignment = Alignment.TopStart
        ) {
            if (loadedLyrics.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.no_lyrics_available),
                    color = Color.White,
                    fontSize = style.fontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = style.lineHeight,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 2
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    items(loadedLyrics?.size ?: 0) { idx ->
                        LyricItem(
                            lyric = loadedLyrics!![idx],
                            index = idx,
                            currentIndex = currentIndex,
                            style = style
                        )
                    }
                }
            }
        }
    }
}


suspend fun fetchLyrics(
    serverUrl: String,
    accessToken: String,
    currentSong: PlaylistItem? = null
): List<LyricLine>? {
    val songId = currentSong?.id ?: return null
    val url = "${serverUrl}music/tracks/$songId/lyrics"
    val client = OkHttpClient()

    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $accessToken")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .build()

    return withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body.string()
                if (body.isBlank()) return@withContext null

                // parse top-level object, then data array
                val root = JSONObject(body)
                if (!root.has("data")) return@withContext null
                val arr = root.getJSONArray("data")

                List(arr.length()) { i ->
                    val obj = arr.getJSONObject(i)
                    // text may be present as empty string
                    val text = obj.optString("text", "")
                    val timeObj = obj.optJSONObject("time")
                    val total = timeObj?.optDouble("total", 0.0) ?: 0.0
                    LyricLine(
                        time = total.toFloat(),
                        text = text
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Lyrics", "fetchLyrics failed", e)
            null
        }
    }
}

suspend fun scrollToItemWithOffsetAndDuration(
    listState: LazyListState,
    targetIndex: Int,
    offsetFromTop: Int,
    durationMillis: Int = 600
) {
    val layoutInfo = listState.layoutInfo
    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }

    val scrollOffset = if (itemInfo != null) {
        itemInfo.offset + offsetFromTop
    } else {
        // Estimate offset if item is not visible
        val averageItemSize = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: return
        (targetIndex - listState.firstVisibleItemIndex) * averageItemSize + offsetFromTop
    }

    val startTime = withFrameNanos { it }
    val durationNanos = durationMillis * 1_000_000L

    var lastTime = startTime
    var scrolled = 0f

    while (true) {
        val currentTime = withFrameNanos { it }
        val elapsed = currentTime - startTime
        val deltaTime = currentTime - lastTime
        lastTime = currentTime

        val progress = (elapsed / durationNanos.toFloat()).coerceIn(0f, 1f)
        val easedProgress = FastOutSlowInEasing.transform(progress)

        val target = scrollOffset * easedProgress
        val delta = target - scrolled
        scrolled += delta

        listState.scrollBy(delta)

        if (progress >= 1f) break
    }
}