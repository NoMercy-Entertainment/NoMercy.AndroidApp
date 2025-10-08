package tv.nomercy.app.shared.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import tv.nomercy.app.shared.models.Rating
import java.util.Locale

@Composable
fun ContentRatingBadge(
    modifier: Modifier = Modifier,
    ratings: List<Rating>,
    size: Dp = 10.dp,
) {
    val isDark = isSystemInDarkTheme()
    val localeCountry = Locale.getDefault().country
    val rating = remember(ratings, localeCountry) {
        ratings.find { it.iso31661 == localeCountry } ?: ratings.firstOrNull()
    }

    if (rating?.iso31661 != null) {
        val imageUrl = "https://pub-a68768bb5b1045f296df9ea56bd53a7f.r2.dev/kijkwijzer/" +
                "${rating.iso31661}/${rating.iso31661}_${rating.rating}.svg"

        val imageLoader = ImageLoader.Builder(LocalContext.current)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        AsyncImage(
            model = imageUrl,
            imageLoader = imageLoader,
            contentDescription = rating.iso31661,
            modifier = modifier
                .size(width = size * 8, height = size * 6),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
    }
}