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
fun NMCarousel(
    modifier: Modifier = Modifier,
    title: String,
    items: List<Component<MediaItem>>,
    navController: NavController, // Accept NavController as a parameter
    moreLink: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = title, modifier = Modifier.weight(1f))
            if (moreLink != null) {
                TextButton(onClick = {
                    navController.navigate(moreLink)
                }) {
                    Text(text = "See more")
                }
            }
        }
        LazyRow {
            items(items) { item ->
                NMCard(
                    mediaItem = item.props.data ?: return@items,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
