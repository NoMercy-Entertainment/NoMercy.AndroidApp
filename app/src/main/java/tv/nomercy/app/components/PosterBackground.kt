package tv.nomercy.app.components

import android.graphics.RuntimeShader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

@Composable
fun PosterBackground(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    images: List<String> = listOf(
        "/prSfAi1xGrhLQNxVSUFh61xQ4Qy.jpg",
        "/pYnRJuBPEqZO1o4fcxBTgmKNHfy.jpg",
        "/eqKNlMgG6pE1tbvTcD86NDNKlru.jpg",
        "/dd5yGBLbqB507gHJSosNY0IYHRQ.jpg",
        "/78lPtwv72eTNqFW9COBYI0dWDJa.jpg",
        "/rWg4Jk7NwVEz2BtU1aKKDoDJPeB.jpg",
        "/f5ZMzzCvt2IzVDxr54gHPv9jlC9.jpg",
        "/4q2NNj4S5dG2RLF9CpXsej7yXl.jpg",
        "/ct5pNE5dDHryHLDnxyZPYcqO1sz.jpg",
    )
) {
    val cardWidth = 120;

    val themeColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.Black.copy(alpha = 0.11f))
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = 0.6f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        radius = 1200f,
                        center = Offset.Zero
                    )
                )
        )

        NoiseOverlay()

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.Center)
                .width(2850.dp)
        ) {

            // Poster stack
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(2850.dp)
                    .horizontalScroll(
                        state = rememberScrollState(((2850 / 2) - floor(cardWidth * 1.3)).toInt()),
                        enabled = false
                    )
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val total = images.size
                images.forEachIndexed { index, image ->
                    val rotation = calculateRotation(index, total)
                    val translation = calculateTranslation(index, total)

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = rotation
                                translationY = translation
                            }
                            .width(cardWidth.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        val imageUrl =
                            "https://app.nomercy.tv/tmdb-images${image}?width=${cardWidth}"

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.5f)
                                            ),
                                            startY = 0f,
                                            endY = size.height
                                        )
                                    )
                                }
                        )
                    }
                }
            }
        }


        // Optional SVG-style noise overlay
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                color = Color.Black.copy(alpha = 0.04f),
                blendMode = BlendMode.Overlay
            )
        }
    }
}

private fun calculateRotation(index: Int, total: Int): Float {
    val center = total / 2
    return -((index - center) * -7f)
}

private fun calculateTranslation(index: Int, total: Int): Float {
    val center = total / 2
    val distance = abs(index - center)
    val maxDistance = center
    val factor = -80f
    return -((distance.toFloat() / maxDistance).pow(2)) * factor * maxDistance
}

private val staticNoiseShader by lazy {
    RuntimeShader(
        """
            uniform float2 resolution;
            half4 main(float2 fragCoord) {
                float2 uv = fragCoord / resolution;
            
                // Multiple seeds for variation
                float seed1 = dot(uv * resolution, float2(12.9898, 78.233));
                float seed2 = dot(uv * resolution, float2(39.3468, 11.1351));
                float seed3 = dot(uv * resolution, float2(25.1234, 66.7890));
            
                float noise1 = fract(sin(seed1) * 43758.5453);
                float noise2 = fract(sin(seed2) * 24693.1234);
                float noise3 = fract(sin(seed3) * 13579.2468);
            
                // Sparse star trigger
                float starGate = step(0.996, noise1); // ~0.4% of pixels
            
                // Size variation (less bias)
                float sizeFactor = pow(noise2, 0.75); // bigger stars
            
                // Brightness variation (less bias)
                float brightness = pow(noise3, 1.3); // more visible
            
                // Final glow (amplified)
                float glow = sizeFactor * brightness * starGate * 3.5;
            
                // Optional halo effect
                glow += smoothstep(0.995, 1.0, noise1) * 1.2;
            
                // Color tint (cool white)
                float3 color = float3(0.85, 0.9, 1.0) * glow;
            
                return half4(color, glow * 0.8);
            }
            """.trimIndent()
    ).apply {
        setFloatUniform("resolution", 2850f, 2850f)
    }
}

private val frozenNoiseBrush by lazy {
    ShaderBrush(staticNoiseShader)
}

@Composable
fun NoiseOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(frozenNoiseBrush)
    )
}