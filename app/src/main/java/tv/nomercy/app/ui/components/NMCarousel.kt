package tv.nomercy.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.MediaItem

@Composable
fun <T> NMCarousel(
    modifier: Modifier,
    component: Component<T>,
    navController: NavController
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = component.props.title, modifier = Modifier.weight(1f))
            if (component.props.moreLink != null) {
                TextButton(onClick = {
                    navController.navigate(component.props.moreLink)
                }) {
                    Text(text = "See more")
                }
            }
        }
        LazyRow {
            item {
                if (component.props.items.isEmpty()) {
                    Text(
                        text = "No items available",
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }
                NMComponent(
                    components = component.props.items,
                    navController = navController,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                )
            }
        }
    }
}
