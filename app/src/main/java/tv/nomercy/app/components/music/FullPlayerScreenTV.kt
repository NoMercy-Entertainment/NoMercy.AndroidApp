package tv.nomercy.app.components.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.dpadFocus
import tv.nomercy.app.shared.utils.pickPaletteColor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreenTV() {
    val context = LocalContext.current
    val musicPlayerStore = remember { GlobalStores.getMusicPlayerStore(context) }

    // collect only what's needed once at the top level
    val currentSong by musicPlayerStore.currentSong.collectAsState()
    val fullPlayerOpen by musicPlayerStore.isFullPlayerOpen.collectAsState()
    val showingLyrics by musicPlayerStore.showingLyrics.collectAsState()

    if (currentSong == null) return

    val fallbackColor = MaterialTheme.colorScheme.primary

    // derived stable value
    val focusColor = remember(currentSong) {
        currentSong?.colorPalette?.cover?.let {
            pickPaletteColor(it, 20, 160, fallbackColor)
        } ?: fallbackColor
    }

    val isDarkMode = remember { mutableStateOf(false) }
    val isVisible = remember { mutableStateOf(true) }
    val lastInteraction = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val playButtonFocusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    fun resetInteraction() {
        lastInteraction.longValue = System.currentTimeMillis()
        isVisible.value = true
    }

    // Animated values derived from the same small set of states
    val artworkSize by animateDpAsState(
        targetValue = when {
            showingLyrics -> 60.dp
            isVisible.value -> 160.dp
            else -> 120.dp
        }, animationSpec = tween(500)
    )

    val titleScale by animateFloatAsState(
        targetValue = when {
            showingLyrics -> 0.5f
            isVisible.value -> 1f
            else -> 0.85f
        }, animationSpec = tween(500)
    )

    val topRowBottomPadding by animateDpAsState(
        targetValue = when {
            showingLyrics -> 0.dp
            isVisible.value -> 150.dp
            else -> 0.dp
        }, animationSpec = tween(500)
    )

    // single auto-hide effect; keyed on lastInteraction
    LaunchedEffect(lastInteraction.longValue) {
        val start = lastInteraction.longValue
        delay(5_000)
        if (lastInteraction.longValue == start) isVisible.value = false
    }

    // request focus when opening or when controls reappear
    LaunchedEffect(fullPlayerOpen, isVisible.value) {
        if (fullPlayerOpen || isVisible.value) {
            // small delay only when opening so system focus settles
            delay(200)
            playButtonFocusRequester.requestFocus()
        }
    }

    Dialog(
        onDismissRequest = { musicPlayerStore.closeFullPlayer() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxSize()
                .focusable()
                .dpadFocus(
                    onBack = {
                        musicPlayerStore.closeFullPlayer()
                        true
                    }
                )
                .onKeyEvent {
                    resetInteraction()
                    false
                }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                focusColor.copy(alpha = 0.7f),
                                focusColor.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(top = 0.dp, bottom = 48.dp, start = 48.dp, end = 48.dp)
                    .fillMaxSize()
            ) {
                // Lyrics layer
                AnimatedVisibility(visible = showingLyrics) {
                    LyricsContainer(
                        isExpanded = true,
                        onToggleExpand = {
                            coroutineScope.launch {
                                delay(40L)
                                bringIntoViewRequester.bringIntoView()
                            }
                        },
                        activeColor = focusColor,
                        bringIntoViewRequester = bringIntoViewRequester,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp, start = 60.dp, end = 60.dp)
                            .bringIntoViewRequester(bringIntoViewRequester)
                    )
                }

                // Top row placement
                TopRowTV(
                    modifier = Modifier
                        .align(if (showingLyrics) Alignment.TopStart else Alignment.BottomStart)
                        .then(if (showingLyrics) Modifier.padding(top = 32.dp) else Modifier.padding(bottom = topRowBottomPadding)),
                    artworkSize = artworkSize,
                    titleScale = titleScale
                )

                // Bottom bar auto-hide
                AnimatedVisibility(
                    visible = isVisible.value,
                    enter = slideInVertically(initialOffsetY = { it * 2 }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    BottomBar(
                        focusColor = focusColor,
                        isDarkMode = isDarkMode,
                        playButtonFocusRequester = playButtonFocusRequester,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TopRowTV(modifier: Modifier, artworkSize: Dp, titleScale: Float) {
    val context = LocalContext.current
    val musicPlayerStore = remember { GlobalStores.getMusicPlayerStore(context) }
    val currentSong by musicPlayerStore.currentSong.collectAsState()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        CoverArtwork(
            item = currentSong!!,
            modifier = Modifier
                .height(artworkSize)
                .aspectRatio(1f)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = currentSong?.name ?: "",
                fontSize = (36.sp * titleScale),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = currentSong?.artistTrack?.joinToString(", ") { it.name } ?: "",
                fontSize = (22.sp * titleScale),
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BottomBar(
    focusColor: Color,
    isDarkMode: MutableState<Boolean>,
    modifier: Modifier,
    playButtonFocusRequester: FocusRequester
) {
    val context = LocalContext.current
    val musicPlayerStore = remember { GlobalStores.getMusicPlayerStore(context) }

    val currentSong by musicPlayerStore.currentSong.collectAsState()
    val timeState by musicPlayerStore.timeState.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.Start
    ) {
        PlayerProgressBar(
            currentTime = timeState.position,
            duration = timeState.duration,
            onSeek = { musicPlayerStore.seekTo(it) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaLikeButton(
                    favorite = currentSong!!.favorite,
                    color = focusColor
                )
            }

            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .height(90.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(24.dp))
                    ShuffleButton(activeColor = focusColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    PreviousButton()
                    Spacer(modifier = Modifier.width(16.dp))
                    StyledPlaybackButton(
                        modifier = Modifier.size(64.dp),
                        backgroundColor = focusColor,
                        focusRequester = playButtonFocusRequester
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    NextButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    RepeatButton(activeColor = focusColor)
                    Spacer(modifier = Modifier.width(24.dp))
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { musicPlayerStore.toggleLyrics() }) {
                    MoooomIcon(icon = MoooomIconName.Lyrics, contentDescription = "Lyrics", tint = Color.White)
                }

                IconButton(onClick = { }) {
                    MoooomIcon(icon = MoooomIconName.CurrentPlaylist, contentDescription = "Current Playlist", tint = Color.White)
                }

                IconButton(onClick = { isDarkMode.value = !isDarkMode.value }) {
                    MoooomIcon(icon = if (isDarkMode.value) MoooomIconName.Sun else MoooomIconName.Moon, contentDescription = "Toggle Dark Mode", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StyledPlaybackButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    focusRequester: FocusRequester
) {
    val context = LocalContext.current
    val musicPlayerStore = remember { GlobalStores.getMusicPlayerStore(context) }
    val isPlaying by musicPlayerStore.isPlaying.collectAsState()

    IconButton(
        modifier = modifier
            .focusRequester(focusRequester)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .onFocusChanged { state ->
                if (state.isFocused) {
                    Modifier.border(2.dp, Color.White, CircleShape)
                } else {
                    Modifier
                }
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            ),
        onClick = { musicPlayerStore.togglePlayback() }
    ) {
        MoooomIcon(
            icon = if (isPlaying) MoooomIconName.NmPause else MoooomIconName.NmPlay,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(36.dp),
            tint = Color.White
        )
    }
}