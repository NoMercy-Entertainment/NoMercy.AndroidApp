package tv.nomercy.app.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import tv.nomercy.app.components.CoverImage
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType

@Composable
fun BackdropImageWithOverlay(imageUrl: String?) {
    Box(modifier = Modifier.fillMaxSize()) {

        Crossfade(
            targetState = imageUrl,
            animationSpec = tween(durationMillis = 300),
            label = "backdrop-fade"
        ) { url ->
            BackdropImage(
                imageUrl = url,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(0f)
            )
        }

        OverlayGradient()
    }
}

@Composable
fun BackdropImage(imageUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 200.dp)
            .fillMaxWidth()
            .aspectFromType(AspectRatio.Backdrop)
            .clipToBounds()
    ) {
        if(imageUrl?.startsWith("/images/music") == true) {
            CoverImage(
                cover = imageUrl,
                name = "Backdrop image for $imageUrl",
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            TMDBImage(
                path = imageUrl,
                title = "Backdrop image for $imageUrl",
                aspectRatio = null,
                size = 1280,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}


@Composable
fun OverlayGradient(offsetModifier: Float = 0f) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val radius = maxOf(widthPx, heightPx) * 1.8f
        val centerOffset = Offset(widthPx - 225, -450f + offsetModifier)

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = 1.15f
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black,
                            Color.Black,
                            Color.Black,
                            Color.Black,
                            Color.Black,
                            Color.Black,
                        ),
                        center = centerOffset,
                        radius = radius
                    )
                )
        )
    }
}
