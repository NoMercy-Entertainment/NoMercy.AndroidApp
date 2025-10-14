package tv.nomercy.app.shared.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.4f),
        Color.Gray.copy(alpha = 0.2f)
    )

    val transition = rememberInfiniteTransition()
    val offsetX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .shadow(elevation = 4.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(offsetX, 0f),
                    end = Offset(offsetX + 200f, 200f)
                )
            )
    )
}

@Composable
fun ShimmerPill(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.4f),
        Color.Gray.copy(alpha = 0.2f)
    )

    val transition = rememberInfiniteTransition()
    val offsetX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .padding(end = 8.dp, bottom = 4.dp)
            .height(28.dp)
            .width(80.dp)
            .clip(RoundedCornerShape(50))
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(offsetX, 0f),
                    end = Offset(offsetX + 200f, 200f)
                )
            )
    )
}

@Composable
fun ShimmerParagraph(
    modifier: Modifier = Modifier,
    lineCount: Int = 3
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(lineCount) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun ShimmerTrackRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Index
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(20.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }

        // Cover + Title + Artist/Album links
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
