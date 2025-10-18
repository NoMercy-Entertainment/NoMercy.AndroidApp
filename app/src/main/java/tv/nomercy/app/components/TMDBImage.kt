package tv.nomercy.app.shared.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType

@Composable
fun TMDBImage(
    modifier: Modifier = Modifier,
    path: String?,
    title: String?,
    aspectRatio: AspectRatio? = null,
    size: Int,
) {
    val imageUrl = if (path != null) "https://app.nomercy.tv/tmdb-images${path}?width=${size}" else null
//    val imageUrl = if (path != null) "https://image.tmdb.org/t/p/w${size}${path}" else null

    val painter = rememberAsyncImagePainter(imageUrl)

    Image(
        painter = painter,
        contentDescription = title ?: "Image",

        modifier = modifier.then(
            if (aspectRatio != null) Modifier
                .aspectFromType(aspectRatio) else Modifier
        ),
        contentScale = ContentScale.Crop
    )
}
