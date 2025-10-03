package tv.nomercy.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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

@Composable
fun NMGrid(
    modifier: Modifier,
    gridItems: List<Component<MediaItem>>
) {
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(padding),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            items(gridItems) { item ->
                NMCard(
                    mediaItem = item.props.data ?: return@items,
                )
            }
        }
    }
}