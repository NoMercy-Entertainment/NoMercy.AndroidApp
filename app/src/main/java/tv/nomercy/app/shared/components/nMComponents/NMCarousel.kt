package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tv.nomercy.app.shared.components.EmptyGrid
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.ComponentData
import tv.nomercy.app.shared.utils.AspectRatio


@Composable
fun <T : ComponentData> NMCarousel(
    component: Component<out T>,
    modifier: Modifier,
    navController: NavController,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
    ) {
        // Header row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Text(
                text = component.props.title,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(4f)
            )

            component.props.moreLink?.let { 
                Box(
                    modifier = Modifier
                        .clickable {
                            navController.navigate(it)
                        }
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = component.props.moreLinkText ?: "See all",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        val items = component.props.items
        val spacing = 8.dp

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = spacing * 2, end = spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            if (items.isEmpty()) {
                item {
                    EmptyGrid(
                        modifier = Modifier.padding(vertical = 16.dp),
                        text = "No items available"
                    )
                }
            } else
                items(items, key = { it.id }) { item ->
                    NMComponent(
                        components = listOf(item),
                        navController = navController,
                        aspectRatio = AspectRatio.Poster
                    )
                }
        }
    }
}
