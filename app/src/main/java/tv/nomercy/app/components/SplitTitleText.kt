package tv.nomercy.app.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import tv.nomercy.app.components.ShimmerBox

@Composable
fun SplitTitleText(
    title: String?,
    modifier: Modifier = Modifier,
    mainStyle: TextStyle = MaterialTheme.typography.titleLarge,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    splitMarkers: List<String> = listOf(":", "–", "—", "-", "and the", "en de", "et le", "und der", "y el")
) {
    if (title.isNullOrBlank()) {
        Column(modifier = modifier) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
        return
    }

    val regex = remember(splitMarkers) {
        Regex("\\s*(${splitMarkers.joinToString("|") { Regex.escape(it) }})\\s+", RegexOption.IGNORE_CASE)
    }

    val match = regex.find(title)
    val (main, subtitle) = if (match != null) {
        val splitIndex = match.range.last
        title.substring(0, splitIndex).trimEnd() to title.substring(splitIndex).trimStart()
    } else {
        title to null
    }

    Column(modifier = modifier) {
        Text(text = main, style = mainStyle)

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = subtitleStyle,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
