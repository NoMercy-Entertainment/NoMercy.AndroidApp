package tv.nomercy.app.components.music

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.utils.adjustBrightness

@Composable
fun EqSpinner(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val barCount = 5
    val baseHeight = 16.dp
    val barWidth = 2.dp
    val barSpacing = 1.dp

    val delays = listOf(600L, 800L, 200L, 0L, 400L)
    val brightnessFactors = listOf(1.3f, 1.7f, 1.2f, 1.5f, 1.1f)

    val animatedHeights = remember { List(barCount) { Animatable(0.6f) } }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animatedHeights.forEachIndexed { i, anim ->
                launch {
                    while (true) {
                        anim.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 400, delayMillis = delays[i].toInt(), easing = FastOutSlowInEasing)
                        )
                        anim.animateTo(
                            targetValue = 0.4f,
                            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                        )
                    }
                }
            }
        } else {
            animatedHeights.forEach { it.snapTo(0.4f) }
        }
    }

    val baseColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .size(baseHeight)
            .clipToBounds(),
        horizontalArrangement = Arrangement.spacedBy(barSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        animatedHeights.forEachIndexed { i, anim ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(anim.value)
                    .shadow(3.dp, shape = RoundedCornerShape(2.dp))
                    .background(
                        color = baseColor.adjustBrightness(brightnessFactors[i]),
                        shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                    )
            )
        }
    }
}