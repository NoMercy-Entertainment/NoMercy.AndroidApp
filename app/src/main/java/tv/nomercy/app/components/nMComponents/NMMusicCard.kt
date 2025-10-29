package tv.nomercy.app.components.nMComponents

import android.view.KeyEvent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import tv.nomercy.app.R
import tv.nomercy.app.components.images.CoverImage
import tv.nomercy.app.shared.api.KeycloakConfig.getSuffix
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMMusicCardProps
import tv.nomercy.app.shared.models.NMMusicCardWrapper
import tv.nomercy.app.shared.models.NMMusicHomeCardProps
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalCurrentItemFocusRequester
import tv.nomercy.app.shared.ui.LocalFocusLeftInRow
import tv.nomercy.app.shared.ui.LocalFocusRightInRow
import tv.nomercy.app.shared.ui.LocalOnActiveCardChange2
import tv.nomercy.app.shared.ui.LocalOnActiveInRow
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.isTv
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun NMMusicCard(
    component: Component,
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {

    val wrapper = component.props as? NMMusicCardWrapper ?: return
    val data = wrapper.data

    val context = LocalContext.current
    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val focusColor: Color = remember(data.colorPalette) {
        if (!useAutoThemeColors) fallbackColor
        else pickPaletteColor(data.colorPalette?.cover, fallbackColor = fallbackColor)
    }

    val footText = remember(data) { buildFootText(data) }
    val (isFocused, isHovered, interactionSource) = rememberTvInteractionState()

    val isTvPlatform = isTv()
    val isActive = isTvPlatform && (isFocused || isHovered)
    val borderWidth by animateDpAsState(if (isActive) 4.dp else 1.dp, label = "border")
    val scale by animateFloatAsState(if (isActive) 1.03f else 1f, label = "scale")

    val onActiveCardChange = LocalOnActiveCardChange2.current
    val onActiveInRow = LocalOnActiveInRow.current

    LaunchedEffect(isActive) {
        if (isActive) {
            onActiveCardChange(data)
            onActiveInRow()
        }
    }

    val focusRequester = LocalCurrentItemFocusRequester.current
    val focusLeft = LocalFocusLeftInRow.current
    val focusRight = LocalFocusRightInRow.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .aspectFromType(AspectRatio.Poster)
            .clickable { navController.navigate(data.link) },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(2f)
                .clip(RoundedCornerShape(6.dp))
                .then(if (useAutoThemeColors) Modifier.paletteBackground(data.colorPalette?.cover) else Modifier)
                .aspectFromType(AspectRatio.Cover)
                .graphicsLayer {
                    if (isTvPlatform) {
                        scaleX = scale
                        scaleY = scale
                    }
                }
                .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
                .then(
                    if (isTvPlatform) Modifier
                        .border(borderWidth, focusColor.copy(alpha = if (isActive) 1f else 0.5f), RoundedCornerShape(6.dp))
                        .focusable(interactionSource = interactionSource)
                        .hoverable(interactionSource = interactionSource)
                        .semantics { role = Role.Button }
                    else if (useAutoThemeColors) Modifier
                        .border(borderWidth, focusColor, RoundedCornerShape(6.dp))
                    else Modifier
                )
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                scope.launch { focusLeft() }; true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                scope.launch { focusRight() }; true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER -> {
                                scope.launch { navController.navigate(data.link) }; true
                            }
                            else -> false
                        }
                    } else false
                },
            shape = RoundedCornerShape(6.dp),
            onClick = { navController.navigate(data.link) }
        ) {
            MusicCardImage(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectFromType(AspectRatio.Cover)
            )
        }

        MusicCardText(
            title = data.name,
            subtitle = footText,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
    }
}

@Composable
private fun MusicCardText(title: String, subtitle: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun rememberTvInteractionState(): Triple<Boolean, Boolean, MutableInteractionSource> {
    val interaction = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    LaunchedEffect(interaction) {
        interaction.interactions.collect { inter ->
            when (inter) {
                is FocusInteraction.Focus -> isFocused = true
                is FocusInteraction.Unfocus -> isFocused = false
                is HoverInteraction.Enter -> isHovered = true
                is HoverInteraction.Exit -> isHovered = false
            }
        }
    }

    return Triple(isFocused, isHovered, interaction)
}

@Composable
fun MusicCardImage(data: NMMusicCardProps, modifier: Modifier) {
    when (data.type) {
        "artists", "albums", "release_groups" -> AlbumBackdrop(data, modifier)
//        "artists" -> ArtistBackdrop(data, currentServer, modifier)
        "playlists" -> PlaylistBackdrop(data, modifier)
        else -> DefaultBackdrop(data, modifier)
    }
}

@Composable
fun MusicHomeCardImage(data: NMMusicHomeCardProps, modifier: Modifier) {

    val newData = NMMusicCardProps(
        id = data.id,
        type = data.type,
        name = data.name,
        link = data.link,
        cover = data.cover,
        colorPalette = data.colorPalette,
//        tracks = data.tracks,
//        year = data.year
    )

    when (data.type) {
        "artists", "albums", "release_groups" -> AlbumBackdrop(newData, modifier)
//        "artists" -> ArtistBackdrop(newData, currentServer, modifier)
        "playlists" -> PlaylistBackdrop(newData, modifier)
        else -> DefaultBackdrop(newData, modifier)
    }
}

@Composable
fun DefaultBackdrop(data: NMMusicCardProps, modifier: Modifier) {

}

@Composable
fun AlbumBackdrop(data: NMMusicCardProps, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        CoverImage(
            data = data,
            modifier = modifier.width(180.dp).align(Alignment.TopCenter)
        )
        if (data.id == "favorite") FavoriteImage()
    }
}

@Composable
fun ArtistBackdrop(data: NMMusicCardProps, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        CoverImage(
            data = data,
            modifier = modifier.width(180.dp).align(Alignment.TopCenter)
        )
        if (data.id == "favorite") FavoriteImage()
    }
}

@Composable
fun PlaylistBackdrop(data: NMMusicCardProps, modifier: Modifier) {

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(180.dp)
                .paletteBackground(data.colorPalette?.cover)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(200.dp)
                .background(Color(0xFFAA88FF).copy(alpha = 0.8f))
        )
        CoverImage(
            data = data,
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
fun FavoriteImage() {
    Icon(
        painter = painterResource(id = R.drawable.heartfilled),
        contentDescription = "Favorite",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
    )
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