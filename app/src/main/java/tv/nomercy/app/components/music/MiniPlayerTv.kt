package tv.nomercy.app.components.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tv.nomercy.app.components.Marquee
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.models.PaletteColors
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.pickPaletteColor


@Composable
fun TvMiniPlayer(
    song: PlaylistItem,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    paletteColors: PaletteColors? = null
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val palette = paletteColors ?: song.colorPalette?.cover
    val backgroundColor = remember(palette, useAutoThemeColors) {
        if (!useAutoThemeColors) fallbackColor // fallback
        else pickPaletteColor(palette, dark = 20, light = 160, fallbackColor)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .height(40.dp)
            .width(360.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.4f),
                        Color.Black.copy(alpha = 0.4f)
                    )
                )
            )
            .clickable { musicPlayerStore.openFullPlayer() }
            .focusable(interactionSource = interactionSource),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
//                    .border(1.dp, Color.Black, CircleShape)
                    .clip(CircleShape)
            ) {
//                CoverImage(
//                    cover = song.cover,
//                    name = song.name,
//                    modifier = Modifier
//                        .fillMaxSize()
//                )
//
                Box(modifier = Modifier
//                    .background(Color.Black.copy(alpha = 0.8f))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.4f),
                            )
                        )
                    )
                    .rotate(-95f)
                    .fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        EqSpinner(modifier = Modifier.height(16.dp))
                    } else {
                        MoooomIcon(
                            icon = MoooomIconName.NmPlay,
                            contentDescription = "Play",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Track info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = song.name,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Marquee {
                    TrackLinksArtists(song.artistTrack, navController)
                }
            }
        }
}