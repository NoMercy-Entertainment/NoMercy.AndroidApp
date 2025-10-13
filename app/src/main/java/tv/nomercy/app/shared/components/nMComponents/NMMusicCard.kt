package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.nomercy.app.R
import tv.nomercy.app.shared.api.KeycloakConfig.getSuffix
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMMusicCardProps
import tv.nomercy.app.shared.models.NMMusicHomeCardProps
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.paletteBackground

@Composable
fun NMMusicCard(
    component: Component,
    modifier: Modifier,
    navController: NavController,
    aspectRatio: AspectRatio? = null,
) {
    val wrapper = component.props as? NMMusicHomeCardProps ?: return
    val data = wrapper.data ?: return

    val serverConfigStore = GlobalStores.getServerConfigStore(LocalContext.current)
    val currentServer = serverConfigStore.currentServer.collectAsState()

    val footText = remember(data) { buildFootText(data) }

    if (data.link != null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable { navController.navigate(data.link) }
                .graphicsLayer {
                    scaleX = 0.98f
                    scaleY = 0.98f
                },
            horizontalAlignment = Alignment.Start
        ) {
            MusicCardImage(
                data = data,
                currentServer = currentServer,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectFromType(aspectRatio)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = data.name.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = footText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MusicCardImage(data: NMMusicCardProps, currentServer: State<Server?>, modifier: Modifier) {
    when (data.type) {
        "artists", "albums", "release_groups" -> AlbumBackdrop(data, currentServer, modifier)
//        "artists" -> ArtistBackdrop(data, currentServer, modifier)
        "playlists" -> PlaylistBackdrop(data, currentServer, modifier)
        else -> DefaultBackdrop(data, modifier)
    }
}

@Composable
fun DefaultBackdrop(data: NMMusicCardProps, modifier: Modifier) {

}

@Composable
fun AlbumBackdrop(data: NMMusicCardProps, currentServer: State<Server?>, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        CoverImage(
            data = data,
            currentServer = currentServer,
            modifier = modifier.width(180.dp).align(Alignment.TopCenter)
        )
        if (data.id == "favorite") FavoriteImage()
    }
}

@Composable
fun ArtistBackdrop(data: NMMusicCardProps, currentServer: State<Server?>, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        CoverImage(
            data = data,
            currentServer = currentServer,
            modifier = modifier.width(180.dp).align(Alignment.TopCenter)
        )
        if (data.id == "favorite") FavoriteImage()
    }
}

@Composable
fun PlaylistBackdrop(data: NMMusicCardProps, currentServer: State<Server?>, modifier: Modifier) {

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(180.dp)
                .paletteBackground(data.colorPalette?.cover)
                .clip(RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(200.dp)
                .background(Color(0xFFAA88FF).copy(alpha = 0.8f))
                .clip(RoundedCornerShape(16.dp))
        )
        CoverImage(
            data = data,
            currentServer = currentServer,
            modifier = modifier.width(180.dp).align(Alignment.TopCenter)
        )

        if (data.id == "favorite") FavoriteImage()
    }
}

fun buildFootText(data: NMMusicCardProps): String {
    val typeLabel = when (data.type) {
        "playlists", "favorites" -> "Playlist"
        "albums" -> "Album"
        "artists" -> "Artist"
        "release_groups" -> "Release"
        "genres" -> "Genre"
        else -> ""
    }

    val trackInfo = data.tracks?.let {
        " • $it ${if (it > 1) "tracks" else "track"}"
    } ?: " • ${data.year ?: ""}"

    return "$typeLabel$trackInfo"
}

@Composable
fun CoverImage(cover: String?, name: String?, currentServer: State<Server?>, modifier: Modifier) {

    val serverBaseUrl = currentServer.value?.let {
        if (it.serverBaseUrl.isNotBlank()) {
            it.serverBaseUrl.trimEnd('/')
        } else {
            null
        }
    }

    val imageUrl = remember(cover, serverBaseUrl) {
        resolveImageUrl(cover, serverBaseUrl)
    }

    if (imageUrl != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Cover image for ${name ?: ""}",
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        AppLogoSquare()
    }
}

@Composable
fun CoverImage(data: NMMusicCardProps, currentServer: State<Server?>, modifier: Modifier) {
    CoverImage(
        cover = data.cover.orEmpty(),
        name = data.name.orEmpty(),
        currentServer = currentServer,
        modifier = modifier
    )
}

@Composable
fun FavoriteImage() {
    Icon(
        painter = painterResource(id = R.drawable.heartfilled),
        contentDescription = "Favorite",
        tint = Color.Red,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun AppLogoSquare(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            tint = Color.White
        )
    }
}

fun resolveImageUrl(
    cover: String?,
    serverBaseUrl: String?
): String? {
    if (cover.isNullOrBlank()) return null

    val suffix = getSuffix()

    return when {
        cover.startsWith("https://") && "fanart.tv" in cover -> {
            val proxy = "https://api$suffix.nomercy.tv/cors?url="
            proxy + cover
        }

        cover.startsWith("https://") -> cover

        cover.startsWith("/") && !serverBaseUrl.isNullOrBlank() -> {
            "$serverBaseUrl${cover}"
        }

        else -> null
    }
}