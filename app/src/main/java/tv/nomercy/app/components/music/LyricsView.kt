package tv.nomercy.app.components.music

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName

// Data class for a lyric line
data class LyricLine(val time: Float, val text: String)

@Composable
fun LyricItem(
    lyric: LyricLine,
    index: Int,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val targetColor = when {
        index < currentIndex -> Color.White
        index == currentIndex -> Color.White
        else -> Color.Black
    }
    val targetAlpha = when {
        index < currentIndex -> 0.5f
        index == currentIndex -> 1f
        else -> 1f
    }
    val animatedColor by animateColorAsState(targetColor, label = "lyricColor")
    val animatedAlpha by animateFloatAsState(targetAlpha, label = "lyricAlpha")
    val fontWeight = FontWeight.Bold

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (lyric.text.isNotEmpty()) {
            Text(
                text = lyric.text,
                color = animatedColor.copy(alpha = animatedAlpha),
                fontSize = 20.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            MoooomIcon(
                icon = MoooomIconName.NoteDouble,
                modifier = Modifier.size(20.dp),
                tint = animatedColor.copy(alpha = animatedAlpha),
                contentDescription = "Music Note"
            )
        }
    }
}

@Composable
fun LyricsView(
    lyrics: List<LyricLine>?,
    currentTime: Long,
    fetchLyrics: (suspend () -> List<LyricLine>? ),
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true // control from parent if needed
) {
    var loadedLyrics by remember { mutableStateOf(lyrics) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var viewportHeight by remember { mutableIntStateOf(0) }

    // Fetch lyrics if not present
    LaunchedEffect(lyrics) {
        if (loadedLyrics == null) {
            loadedLyrics = fetchLyrics()
        }
    }

    // Convert currentTime to seconds (Float) if needed
    val currentTimeSec = remember(currentTime) { currentTime / 1000f }

    // --- Robust currentIndex calculation as derived state ---
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

    LaunchedEffect(currentIndex, viewportHeight) {
        if (isExpanded && currentIndex >= 0 && currentIndex != lastScrolledIndex && viewportHeight > 0) {
            lastScrolledIndex = currentIndex
            coroutineScope.launch {
                delay(16)
                val offset = (viewportHeight / 3)
                listState.animateScrollToItem(currentIndex.coerceAtLeast(0), -offset)
            }
        }
    }

    // --- Lyrics List Only, minimal margin ---
    if (isExpanded) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    viewportHeight = coordinates.size.height
                },
            contentAlignment = Alignment.Center
        ) {
            if (loadedLyrics.isNullOrEmpty()) {
                Text("No lyrics available", color = Color.Black, fontSize = 18.sp, textAlign = TextAlign.Start)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    items(loadedLyrics!!.size) { idx ->
                        val line = loadedLyrics!![idx]
                        LyricItem(
                            lyric = line,
                            index = idx,
                            currentIndex = currentIndex
                        )
                    }
                }
            }
        }
    }
}
