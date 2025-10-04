package tv.nomercy.app.shared.utils

import android.R.attr.background
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.colorResource
import tv.nomercy.app.R
import tv.nomercy.app.shared.models.PaletteColors

enum class AspectRatio {
    Poster,
    Backdrop
}

fun Modifier.aspectFromType(aspectRatio: AspectRatio?): Modifier {
    return when (aspectRatio) {
        AspectRatio.Poster -> this.aspectRatio(2f / 3f)
        AspectRatio.Backdrop -> this.aspectRatio(16f / 9f)
        else -> this // no aspect applied
    }
}

@Composable
fun Modifier.paletteBackground(palette: PaletteColors?): Modifier {
    if (palette == null) return background(MaterialTheme.colorScheme.primary)

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

    val top = colorResource(id = R.color.theme_8)
    val bottom = colorResource(id = R.color.theme_5)

    return Modifier.drawWithCache() {
        val radial1 = Brush.radialGradient(
            colors = listOf(top.copy(alpha = 0.2f), bottom.copy(alpha = 0f)),
            center = Offset(size.width * 0.63f, size.height * -0.0315f),
            radius = size.minDimension * 0.8f
        )

        val radial2 = Brush.radialGradient(
            colors = listOf(top.copy(alpha = 0.2f), bottom.copy(alpha = 0f)),
            center = Offset(size.width * 1.015f, size.height * -0.0298f),
            radius = size.minDimension * 0.7f
        )

        val linear = Brush.verticalGradient(
            colors = listOf(top.copy(alpha = 0.2f), bottom.copy(alpha = 0f))
        )

        onDrawBehind {
            drawRect(radial1)
            drawRect(radial2)
            drawRect(linear)
        }
    }
}
