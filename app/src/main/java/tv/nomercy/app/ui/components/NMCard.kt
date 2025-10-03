
package tv.nomercy.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.lib.parseRgbColor
import tv.nomercy.app.lib.pickPaletteColor

@Composable
fun NMCard(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem
) {
    val navController = NavController(LocalContext.current);

    var isFocused by remember { mutableStateOf(false) }

    val palette = mediaItem.colorPalette?.get("poster")
    val focusColor = parseRgbColor(pickPaletteColor(palette))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2 / 3f)
            .onFocusChanged { isFocused = it.isFocused },
        border = if (isFocused && focusColor != null) BorderStroke(2.dp, focusColor) else null,
        onClick = {
//            navController.navigate(mediaItem.link)
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TMDBImage(
                path = mediaItem.poster,
                title = mediaItem.title ?: mediaItem.name,
                aspect = "poster",
                size = 180
            )

        }
    }
}
