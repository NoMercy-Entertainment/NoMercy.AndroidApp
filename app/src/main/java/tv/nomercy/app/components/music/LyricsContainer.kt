@file:Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE", "UNUSED_IMPORT")

package tv.nomercy.app.components.music

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import tv.nomercy.app.R
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.isTv


@Composable
fun LyricsContainer(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    height: Dp? = null,
    onToggleExpand: () -> Unit,
    activeColor: Color = Color.White,
    onHeaderHeightMeasured: (Dp) -> Unit = {},
    bringIntoViewRequester: BringIntoViewRequester? = null
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val currentSong by musicPlayerStore.currentSong.collectAsState()
    val timeState by musicPlayerStore.timeState.collectAsState()

    val density = LocalDensity.current
    val isTv = isTv()

    val animatedLyricsRadius by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 300),
        label = "animatedLyricsRadius"
    )

    Column(
        modifier = modifier
            .then(
                if (height != null) Modifier.height(height) else Modifier
            )
            .clip(RoundedCornerShape(animatedLyricsRadius))
            .background( if(isTv) Color.Transparent else activeColor.copy(alpha = 0.8f))
            .zIndex(2f)
    ) {

        if(!isTv) {
            // build header modifier and attach bringIntoViewRequester if provided
            val headerModifier = remember(bringIntoViewRequester) {
                var m = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                if (bringIntoViewRequester != null) m = m.bringIntoViewRequester(bringIntoViewRequester)
                m
            }
            Row(
                modifier = headerModifier.onGloballyPositioned { coords ->
                    // report header height in Dp back to the parent
                    val hPx = coords.size.height.toFloat()
                    val hDp = with(density) { hPx.toDp() }
                    onHeaderHeightMeasured(hDp)
                },
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
                    contentDescription = if (isExpanded) stringResource(id = R.string.collapse) else stringResource(
                        id = R.string.expand
                    ),
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable {
                            onToggleExpand()
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0f),
                            Color.Black.copy(alpha = 0.2f)
                        )
                    )
                )
                .then(if(isTv) Modifier.padding(top = 52.dp, bottom = 24.dp) else Modifier.padding(start = 16.dp, end = 16.dp))

                .weight(1f)
        ) {
            LyricsView(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}
