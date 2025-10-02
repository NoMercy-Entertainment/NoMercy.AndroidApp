package tv.nomercy.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.MediaItem
import kotlin.math.ceil

@Composable
fun NMGrid(
    modifier: Modifier,
    gridItems: List<Component<MediaItem>>

) {
    BoxWithConstraints(modifier = modifier) {
        val columns = 2
        val padding = 16.dp
        val spacing = 16.dp

        if (gridItems.isEmpty()) {
            // Show empty state
            Text(
                text = "No items available",
                modifier = modifier
                    .padding(padding)
            )
        }
        else {
            val itemWidth = (this.maxWidth - spacing * (columns - 1)) / columns
            val itemHeight = itemWidth * 3 / 2 // 2:3 aspect ratio

            val rows = ceil(gridItems.size.toFloat() / columns).toInt()

            val adjustedItemHeight = itemHeight - spacing
            val totalHeight = adjustedItemHeight * rows

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .height(totalHeight),
                contentPadding = PaddingValues(padding),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                items(gridItems) { item ->
                    NMCard(
                        mediaItem = item.props.data ?: return@items,
                    )
                }
            }
        }
    }
}