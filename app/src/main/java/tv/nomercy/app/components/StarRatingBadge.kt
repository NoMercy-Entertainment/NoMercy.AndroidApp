package tv.nomercy.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.nomercy.app.shared.utils.round

@Composable
fun RatingBadge(
    rating: Double?,
    modifier: Modifier = Modifier,
    maxRating: Float = 10f,
    starSize: Dp = 16.dp,
    filledColor: Color = Color(0xFFFFD700), // gold
    emptyColor: Color = Color.Gray,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium)
) {
    if (rating == null) return

    val fillFraction = (rating / maxRating).coerceIn(0.0, 1.0)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Canvas(modifier = Modifier.size(starSize)) {
                val starPath = Path().apply {
                    val w = size.width
                    val h = size.height
                    moveTo(w / 2f, 0f)
                    lineTo(w * 0.65f, h * 0.35f)
                    lineTo(w, h * 0.4f)
                    lineTo(w * 0.75f, h * 0.65f)
                    lineTo(w * 0.8f, h)
                    lineTo(w / 2f, h * 0.8f)
                    lineTo(w * 0.2f, h)
                    lineTo(w * 0.25f, h * 0.65f)
                    lineTo(0f, h * 0.4f)
                    lineTo(w * 0.35f, h * 0.35f)
                    close()
                }

                // Draw empty star
                drawPath(path = starPath, color = emptyColor)

                // Clip and draw filled portion
                clipPath(starPath) {
                    drawRect(
                        color = filledColor,
                        size = Size((size.width * fillFraction).toFloat(), size.height)
                    )
                }
            }

            Text(
                text = "${rating.round(1)}/10",
                style = textStyle
            )
        }
    }
}