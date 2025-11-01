package tv.nomercy.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun Indexer(
    modifier: Modifier,
    // explicit flows (preferred for all usages)
    showIndexerState: StateFlow<Boolean>? = null,
    selectedIndexState: StateFlow<Int>? = null,
    activeLettersState: StateFlow<Set<Char>>? = null,
    onIndexSelectedCallback: ((Char) -> Unit)? = null
) {
    // If we don't have any source, nothing to show
    if (showIndexerState == null || selectedIndexState == null || activeLettersState == null) return

    val showIndexer by showIndexerState.collectAsState()
    val selectedIndex by selectedIndexState.collectAsState()
    val activeLetters by activeLettersState.collectAsState()

    val characters = listOf('#') + ('A'..'Z').toList()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(end = 4.dp, top = 16.dp, bottom = 16.dp)
            .width(if (!showIndexer) 0.dp else 32.dp)
            .alpha(if (showIndexer) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        characters.forEachIndexed { index, char ->
            val isSelected = selectedIndex == index
            val isActive = activeLetters.contains(char)
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent
            val textColor = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape.copy(CornerSize(5.dp)))
                    .background(backgroundColor)
                    .clickable(enabled = showIndexer && isActive) {
                        // invoke explicit callback when provided
                        onIndexSelectedCallback?.invoke(char)
                    }
                    .alpha(if (isActive) 1f else 0.3f),
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
