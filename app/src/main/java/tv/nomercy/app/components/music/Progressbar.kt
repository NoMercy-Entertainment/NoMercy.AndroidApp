package tv.nomercy.app.components.music

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt


@Composable
fun FlatSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    barHeight: Dp = 8.dp,
    cornerRadius: Dp = 50.dp
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(value) }

    val coercedValue = dragPosition.coerceIn(valueRange.start, valueRange.endInclusive)

    LaunchedEffect(value, isDragging) {
        if (!isDragging) {
            dragPosition = value
        }
    }

    Box(
        modifier = modifier
            .height(barHeight * 2)
            .pointerInput(valueRange, isDragging) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        val width = size.width
                        val percent = (change.position.x / width).coerceIn(0f, 1f)
                        dragPosition = valueRange.start + percent * (valueRange.endInclusive - valueRange.start)
                        onValueChange(dragPosition)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished()
                    },
                    onDragCancel = {
                        isDragging = false
                        onValueChangeFinished()
                    }
                )
            }
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val width = size.width
                    val percent = (offset.x / width).coerceIn(0f, 1f)
                    dragPosition = valueRange.start + percent * (valueRange.endInclusive - valueRange.start)
                    onValueChange(dragPosition)
                    onValueChangeFinished()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width
            val barY = size.height / 2
            val progress = ((coercedValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            drawRoundRect(
                color = backgroundColor,
                topLeft = Offset(0f, barY - barHeight.toPx() / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, barY - barHeight.toPx() / 2),
                size = androidx.compose.ui.geometry.Size(barWidth * progress, barHeight.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }
    }
}

@Composable
fun PlayerProgressBar(
    currentTime: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var timeChoice by rememberSaveable { mutableStateOf("remaining") }
    val displayCurrent = humanTime(currentTime)
    val displayEnd = if (timeChoice == "duration") humanTime(duration) else humanTime(duration - currentTime)

    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember(currentTime, duration) { mutableFloatStateOf(currentTime.toFloat()) }
    LaunchedEffect(currentTime, isDragging) {
        if (!isDragging) {
            sliderPosition = currentTime.toFloat()
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = displayCurrent,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.widthIn(min = 48.dp),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start
        )
        FlatSeekBar(
            value = sliderPosition,
            onValueChange = {
                isDragging = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek(sliderPosition.roundToInt().toLong())
            },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            barColor = Color.White,
            backgroundColor = Color.White.copy(alpha = 0.2f),
            barHeight = 8.dp,
            cornerRadius = 50.dp
        )
        Text(
            text = displayEnd,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .widthIn(min = 48.dp)
                .clickable { timeChoice = if (timeChoice == "duration") "remaining" else "duration" },
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End
        )
    }
}

fun humanTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
