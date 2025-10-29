package tv.nomercy.app.components.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.disk.DiskCache
import coil3.disk.directory
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
    val context = LocalContext.current

    val imageUrl = if (path != null) "https://app.nomercy.tv/tmdb-images${path}?width=${size}" else null
//    val imageUrl = if (path != null) "https://image.tmdb.org/t/p/w${size}${path}" else null

    val imageLoader = remember { ImageLoader.Builder(context)
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .build()}

    AsyncImage(
        model = imageUrl,
        contentDescription = title ?: "Image",
        imageLoader = imageLoader,
        modifier = modifier.then(
            if (aspectRatio != null) Modifier
                .aspectFromType(aspectRatio) else Modifier
        ),
        contentScale = ContentScale.Crop
    )
}
