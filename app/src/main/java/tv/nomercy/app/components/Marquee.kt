package tv.nomercy.app.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Marquee(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val fadeAnim = remember { Animatable(1f) }
    val contentWidth = remember { mutableIntStateOf(0) }
    val containerWidth = remember { mutableIntStateOf(0) }

    val scrollPadding = with(LocalDensity.current) { 16.dp.toPx() }
    val pauseDuration = 1500L
    val fadeDuration = 500

    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .onGloballyPositioned { layoutCoordinates ->
                containerWidth.intValue = layoutCoordinates.size.width
            }
            .graphicsLayer { alpha = fadeAnim.value }
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState, enabled = false)
                .padding(end = 16.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    contentWidth.intValue = layoutCoordinates.size.width
                },
            content = content
        )
    }

    LaunchedEffect(contentWidth.intValue, containerWidth.intValue) {
        if (contentWidth.intValue <= containerWidth.intValue) return@LaunchedEffect

        while (true) {
            // Start paused
            delay(pauseDuration)

            val maxScroll = (contentWidth.intValue - containerWidth.intValue + scrollPadding).toInt()
            scrollState.animateScrollTo(
                value = maxScroll,
                animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
            )

            // Pause at end
            delay(pauseDuration)

            // Fade out
            fadeAnim.animateTo(0f, tween(fadeDuration))

            // Reset scroll
            scrollState.scrollTo(0)

            // Fade back in
            fadeAnim.animateTo(1f, tween(fadeDuration))
        }
    }
}