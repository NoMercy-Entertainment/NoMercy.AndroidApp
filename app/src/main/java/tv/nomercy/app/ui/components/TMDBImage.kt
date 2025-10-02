
package tv.nomercy.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@Composable
fun TMDBImage(
    modifier: Modifier = Modifier,
    path: String?,
    title: String?,
    aspect: String?,
    size: Int
) {

    val imageUrl = if (path != null) "https://app.nomercy.tv/tmdb-images${path}?width=${size}" else null
//    val imageUrl = if (path != null) "https://image.tmdb.org/t/p/w${size}${path}" else null

    Image(
        painter = rememberAsyncImagePainter(model = imageUrl),
        contentDescription = title ?: "Image",
        modifier = modifier
            .fillMaxSize()
            .then(
                if (aspect != null) {
                    Modifier.aspectRatio(if (aspect == "poster") 2 / 3f else 16 / 9f)
                } else {
                    Modifier
                }
            ),
        contentScale = ContentScale.Crop
    )
}
