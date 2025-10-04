package tv.nomercy.app.shared.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.nomercy.app.shared.utils.AspectRatio

@Composable
fun EmptyGrid(modifier: Modifier, text: String) {
    BoxWithConstraints(modifier = modifier) {
        val columns = 2
        val padding = 16.dp
        val spacing = 16.dp

        val itemWidth = (this.maxWidth - spacing * (columns - 1)) / columns
        val itemHeight = itemWidth * 3 / 2 // 2:3 aspect ratio

        val rows = 2

        val adjustedItemHeight = itemHeight - spacing
        val totalHeight = adjustedItemHeight * rows

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .height(totalHeight)
                .padding(padding),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            items(1) { item ->
                Card(
                    modifier = modifier
                        .fillMaxWidth()
                        .aspectRatio(2 / 3f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        TMDBImage(
                            path = null,
                            title = text,
                            aspectRatio = AspectRatio.Poster,
                            size = 180
                        )

                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}