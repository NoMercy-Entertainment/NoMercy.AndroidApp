package tv.nomercy.app.shared.components.NMComponents

import ComponentData
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component

@Composable
fun <T: ComponentData> NMCarousel(
    component: Component<T>,
    modifier: Modifier,
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
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                )
            }
        }
    }
}
