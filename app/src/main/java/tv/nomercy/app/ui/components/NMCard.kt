package tv.nomercy.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.lib.pickPaletteColor

@Composable
fun <T> NMCard(
    modifier: Modifier,
    component: Component<T>,
    navController: NavController
) {
    component.props.data as MediaItem

    val focusColor: Color = remember(component.props.data.colorPalette) {
        val palette = component.props.data.colorPalette?.poster
        val color = pickPaletteColor(palette);
        color
    }

    println("Picked color: $focusColor from ${component.props.data.colorPalette}")


    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2 / 3f),
        border = BorderStroke(2.dp, focusColor),
        onClick = {
//            component.props.data.link.let { navController.navigate(it) }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TMDBImage(
                path = component.props.data.poster,
                title = component.props.data.title ?: component.props.data.name,
                aspect = "poster",
                size = 180
            )
        }
    }
}
