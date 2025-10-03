package tv.nomercy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Indexer(
    isEnabled: Boolean,
    selectedIndex: Int,
    onIndexSelected: (Char) -> Unit
) {
    val characters = listOf('#') + ('A'..'Z').toList()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
            .alpha(if (isEnabled) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        characters.forEachIndexed { index, char ->
            val isSelected = selectedIndex == index
            val backgroundColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val textColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(enabled = isEnabled) { onIndexSelected(char) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char.toString(),
                    textAlign = TextAlign.Center,
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
