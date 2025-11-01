package tv.nomercy.app.components.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.disk.DiskCache
import coil3.disk.directory
import tv.nomercy.app.components.nMComponents.resolveImageUrl
import tv.nomercy.app.shared.models.NMMusicCardProps
import tv.nomercy.app.shared.stores.GlobalStores


@Composable
fun CoverImage(cover: String?, name: String?, modifier: Modifier) {
    val context = LocalContext.current

    val serverConfigStore = GlobalStores.getServerConfigStore(LocalContext.current)
    val currentServer by serverConfigStore.currentServer.collectAsState()

    val serverBaseUrl = currentServer?.let {
        if (it.serverBaseUrl.isNotBlank()) {
            it.serverBaseUrl.trimEnd('/')
        } else {
            null
        }
    }

    val imageUrl = remember(cover, serverBaseUrl) {
        resolveImageUrl(cover, serverBaseUrl)
    }

    val imageLoader = remember {
        ImageLoader
            .Builder(context)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }

    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            imageLoader = imageLoader,
            contentDescription = "Cover image for ${name ?: ""}",
            modifier = modifier
                .shadow(
                    elevation = 4.dp,
                ),
            contentScale = ContentScale.Crop
        )
    } else {
        AppLogoSquare(modifier)
    }
}

@Composable
fun CoverImage(data: NMMusicCardProps, modifier: Modifier) {
    CoverImage(
        cover = data.cover.orEmpty(),
        name = data.name,
        modifier = modifier
    )
}