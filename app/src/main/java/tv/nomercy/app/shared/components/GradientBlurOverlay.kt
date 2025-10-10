package tv.nomercy.app.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun GradientBlurOverlay(
    baseColor: Color,
    modifier: Modifier = Modifier,
    layerCount: Int = 6,
    maxHeight: Dp = 122.dp,
    maxBlur: Dp = 3.dp,
    alpha: Float = 0.12f
) {
    val layerHeightStep = maxHeight / layerCount
    val blurStep = maxBlur / layerCount

    Box(modifier = modifier
        .height(maxHeight)
        .blur(12.dp)
        .fillMaxWidth()
        .zIndex(0f)
    ) {
        repeat(layerCount) { index ->
            val height = maxHeight - (layerHeightStep * index)
            val blur = blurStep * (index + 1)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .align(Alignment.BottomStart)
                    .graphicsLayer {
                        renderEffect = android.graphics.RenderEffect
                            .createBlurEffect(
                                blur.toPx(),
                                blur.toPx(),
                                android.graphics.Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                    .background(baseColor.copy(alpha = alpha))
            )
        }
    }
}