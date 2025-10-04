package tv.nomercy.app.shared.components.NMComponents

import ComponentData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.shared.components.TMDBImage
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.MediaItem
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun <T: ComponentData> NMCard(
    component: Component<T>,
    modifier: Modifier,
    navController: NavController,
    aspectRatio: AspectRatio? = null,
) {
    val data = component.props.data ?: return

    if (data !is MediaItem) {
        println("NMCard received unexpected data type: ${data::class.simpleName}")
        return
    }

    val focusColor: Color = remember(data.colorPalette) {
        val palette = data.colorPalette?.poster
        val color = pickPaletteColor(palette);
        color
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectFromType(aspectRatio),
        border = BorderStroke(2.dp, focusColor.copy(alpha = 0.5f)),
        onClick = {
//            datalink.let { navController.navigate(it) }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()
            .paletteBackground(data.colorPalette?.poster)) {
            TMDBImage(
                path = data.poster,
                title = data.title,
                aspectRatio = AspectRatio.Poster,
                size = 180,
            )
        }
    }
}
