package tv.nomercy.app.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
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

    val painter = rememberAsyncImagePainter(model = imageUrl)

    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Image(
            painter = painter,
            contentDescription = title ?: "Image",
            modifier = modifier
                .fillMaxSize()
                .aspectFromType(aspectRatio),
            contentScale = ContentScale.Crop
        )
    }
}
