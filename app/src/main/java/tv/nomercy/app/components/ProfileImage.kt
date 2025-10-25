package tv.nomercy.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.nomercy.app.R
import tv.nomercy.app.views.base.auth.shared.AuthViewModel
import tv.nomercy.app.views.base.auth.shared.AuthViewModelFactory
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.aspectFromType

/**
 * Generic profile image component that can be reused across mobile and TV.
 * - Renders the user's avatar if available, otherwise a colored circle fallback.
 * - Draws a small online indicator dot (uses theme colors) on the top-right by default.
 * - Provides sizing, padding, border customization, and optional avatar override.
 */
@Composable
fun ProfileImage(
    modifier: Modifier = Modifier,
    showPresenceDot: Boolean = false,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    avatarOverrideUrl: String? = null,
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val userInfo by authViewModel.userInfo.collectAsState()

    val appConfigStore = GlobalStores.getAppConfigStore(context)
    val userProfile by appConfigStore.userProfile.collectAsState()

    val resolvedAvatarUrl = avatarOverrideUrl
        ?: userProfile?.avatarUrl?.takeIf { it.isNotBlank() }
        ?: userInfo?.avatarUrl?.takeIf { it.isNotBlank() }
        ?: userInfo?.email?.let { email ->
            "https://www.gravatar.com/avatar/${email.hashCode()}?d=retro&s=128"
        }

    val imageLoader = ImageLoader.Builder(context)
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .build()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
    ) {
        if (!resolvedAvatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = resolvedAvatarUrl,
                contentDescription = userInfo?.username,
                imageLoader = imageLoader,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(borderWidth, borderColor, shape = CircleShape),
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.user),
                error = painterResource(R.drawable.user)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    .border(borderWidth, borderColor, shape = CircleShape)
            )
        }

        if (showPresenceDot) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .offset(x = 4.dp, y = (-4).dp)
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                    .border(2.dp, Color.White, shape = CircleShape)
            )
        }
    }
}
