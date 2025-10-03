package tv.nomercy.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Indexer(
    isVisible: Boolean,
    selectedIndex: Int,
    onIndexSelected: (Char) -> Unit
) {
    val characters = listOf('#') + ('A'..'Z').toList()

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .clickable { onIndexSelected(char) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        textAlign = TextAlign.Center,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}
