package tv.nomercy.app.components.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.rememberSwipeableState
import tv.nomercy.app.components.Marquee
import tv.nomercy.app.components.CoverImage
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.pickPaletteColor
import kotlin.math.abs

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun MiniPlayer(
    onStopPlayback: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isOpen: Boolean
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    val currentSong = musicPlayerStore.currentSong.collectAsState().value

    if(currentSong == null) {
        // No song is playing, don't show the mini player
        return
    }

    val percentage = musicPlayerStore.timeState.collectAsState().value.percentage

    val useAutoThemeColors = true // TODO: from settings

    val fallbackColor = MaterialTheme.colorScheme.primary
    val palette = currentSong.colorPalette?.cover
    val focusColor = remember(palette, useAutoThemeColors) {
        if (!useAutoThemeColors) Color(0xFF444444) // fallback
        else pickPaletteColor(palette, dark = 20, light = 160, fallbackColor)
    }

    val containerWidth = remember { mutableFloatStateOf(0f) }
    val offsetX = remember { mutableFloatStateOf(0f) }
    val opacity = remember { mutableFloatStateOf(1f) }

    val swipeableState = rememberSwipeableState(initialValue = 0)

    if (isOpen) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { musicPlayerStore.openFullPlayer() }
                .onGloballyPositioned {
                    containerWidth.floatValue = it.size.width.toFloat()
                }
                .graphicsLayer {
                    translationX = offsetX.floatValue
                    alpha = opacity.floatValue
                }
        ) {
            // Gradient overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                focusColor.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverImage(
                        cover = currentSong.cover,
                        name = currentSong.name,
                        modifier = Modifier
                            .size(40.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentSong.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Marquee {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TrackLinksArtists(
                                    artists = currentSong.artistTrack,
                                    navController = navController
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
//                DeviceButton()
                    MediaLikeButton(favorite = currentSong.favorite, color = focusColor)

                    PlaybackButton()
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black.copy(alpha = 0.22f))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(fraction = percentage)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.05f), focusColor)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset {
                        IntOffset((containerWidth.floatValue * percentage).toInt(), 0)
                    }
                    .size(width = 1.dp, height = 1.dp)
                    .background(Color(0xFFF1EEFE))
            )
        }

        // Swipe logic
        LaunchedEffect(offsetX.floatValue) {
            val length = abs(offsetX.floatValue)
            if (containerWidth.floatValue > 0) {
                opacity.floatValue = 1.1f - (length / containerWidth.floatValue)
            }
        }

        LaunchedEffect(swipeableState.currentValue) {
            if (swipeableState.currentValue == 1) {
                offsetX.floatValue = containerWidth.floatValue
                opacity.floatValue = 0f
                onStopPlayback()
            } else {
                offsetX.floatValue = 0f
                opacity.floatValue = 1f
            }
        }
    }
}
