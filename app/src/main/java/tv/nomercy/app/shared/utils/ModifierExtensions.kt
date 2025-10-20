package tv.nomercy.app.shared.utils

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.nomercy.app.R
import tv.nomercy.app.shared.models.PaletteColors

enum class AspectRatio {
    Poster,
    Backdrop,
    Logo,
    Profile,
    Cover,
}

fun AspectRatio.ratio(): Float = when (this) {
    AspectRatio.Poster -> 2f / 3f
    AspectRatio.Backdrop -> 16f / 9f
    AspectRatio.Logo -> 4f / 1f
    AspectRatio.Profile -> 1f
    AspectRatio.Cover -> 1f
}

fun Modifier.aspectFromType(aspectRatio: AspectRatio?): Modifier {
    return when (aspectRatio) {
        AspectRatio.Poster -> this.aspectRatio(2f / 3f)
        AspectRatio.Backdrop -> this.aspectRatio(16f / 9f)
        AspectRatio.Cover -> this.aspectRatio(1f)
        else -> this // no aspect applied
    }
}

@Composable
fun Modifier.paletteBackground(palette: PaletteColors?): Modifier {
    if (palette == null) return background(MaterialTheme.colorScheme.primary)
    if (palette.dominant == null || palette.lightVibrant == null || palette.darkVibrant == null) {
        return background(MaterialTheme.colorScheme.primary)
    }

    val topLeft = palette.lightVibrant.toColor().copy(alpha = 0.75f)
    val topRight = palette.dominant.toColor().copy(alpha = 0.70f)
    val bottomLeft = palette.darkVibrant.toColor().copy(alpha = 0.80f)
    val bottomRight = palette.dominant.toColor().copy(alpha = 0.70f)

    return drawBehind {
        val radius = size.maxDimension * 0.9f

        drawRadialCornerGradient(topLeft, Offset(0f, 0f), radius)
        drawRadialCornerGradient(topRight, Offset(size.width, 0f), radius)
        drawRadialCornerGradient(bottomLeft, Offset(0f, size.height), radius)
        drawRadialCornerGradient(bottomRight, Offset(size.width, size.height), radius)

        // ✨ White glow overlay (matches your CSS radial-gradient)
        val glowRadius = size.maxDimension * 1.43f
        val glowCenter = Offset(-size.width * 0.1f, -size.height * 0.1f)

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = glowCenter,
                radius = glowRadius
            ),
            size = size
        )
    }
}

private fun DrawScope.drawRadialCornerGradient(color: Color, center: Offset, radius: Float) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius
        ),
        size = size
    )
}

@Composable
fun gradientButtonBackground(active: Boolean = false): Modifier {

    val top = MaterialTheme.colorScheme.primary
    val bottom = MaterialTheme.colorScheme.primary

    return Modifier.drawWithCache {
        val radial1 = Brush.radialGradient(
            colors = listOf(top.copy(alpha = 0.8f), bottom.copy(alpha = 0.4f)),
            center = Offset(size.width * 0.63f, size.height * -0.0315f),
            radius = size.minDimension * 0.8f
        )

        val radial2 = Brush.radialGradient(
            colors = listOf(top.copy(alpha = 0.6f), bottom.copy(alpha = 0.2f)),
            center = Offset(size.width * 1.015f, size.height * -0.0298f),
            radius = size.minDimension * 0.7f
        )

        val linear = Brush.verticalGradient(
            colors = listOf(top.copy(alpha = 0.4f), bottom.copy(alpha = 0.1f))
        )

        onDrawBehind {
            drawRect(radial1)
            drawRect(radial2)
            drawRect(linear)
        }
    }
}

fun Modifier.assertBoundedWidth(): Modifier = layout { measurable, constraints ->
    val isBounded = constraints.maxWidth != Constraints.Infinity
    require(isBounded) {
        "NMGrid must be wrapped in a width-constrained parent (e.g., Modifier.fillMaxWidth())"
    }
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}


enum class SnapAnchor {
    Top,
    Bottom,
    Left,
    Right,
}

suspend fun ScrollState.snapToOffset(
    targetOffset: Int,
    anchor: SnapAnchor = SnapAnchor.Top,
    density: Density,
    configuration: Configuration
) {
    val viewportHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }
    val viewportWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    val snapOffset = when (anchor) {
        SnapAnchor.Top -> targetOffset.coerceAtLeast(0)
        SnapAnchor.Bottom -> (targetOffset - viewportHeightPx).coerceAtLeast(0)
        SnapAnchor.Left -> targetOffset.coerceAtLeast(0)
        SnapAnchor.Right -> (targetOffset - viewportWidthPx).coerceAtLeast(0)
    }

    animateScrollTo(snapOffset.coerceIn(0, this.maxValue))
}

suspend fun LazyListState.snapToOffset(
    targetOffset: Int,
    anchor: SnapAnchor,
    density: Density,
    configuration: Configuration
) {
    val viewportHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }
    val viewportWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    val snapOffset = when (anchor) {
        SnapAnchor.Top -> targetOffset.coerceAtLeast(0)
        SnapAnchor.Bottom -> (targetOffset - viewportHeightPx).coerceAtLeast(0)
        SnapAnchor.Left -> targetOffset.coerceAtLeast(0)
        SnapAnchor.Right -> (targetOffset - viewportWidthPx).coerceAtLeast(0)
    }

    this.scrollToItem(0, snapOffset)
}

fun Modifier.onSubtreeFocusChanged(
    onFocusChanged: (Boolean) -> Unit
): Modifier = this.then(
    Modifier
        .focusGroup()
        .then(
            Modifier.onFocusEvent { event ->
                onFocusChanged(event.hasFocus)
            }
        )
)