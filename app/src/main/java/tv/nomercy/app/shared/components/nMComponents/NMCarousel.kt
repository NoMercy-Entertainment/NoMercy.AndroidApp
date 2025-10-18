package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMCarouselProps
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.isTv

@Composable
fun NMCarousel(
    component: Component,
    modifier: Modifier = Modifier,
    navController: NavController,
    visibleCards: Int = if (isTv()) 7 else 3,
    peekFraction: Float = if (isTv()) 0.6f else 0.25f,
) {
    val props = component.props as? NMCarouselProps ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Header row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTv()) 36.dp else 52.dp)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (isTv()) 4.dp else 12.dp,
                    bottom = 4.dp
                )
        ) {
            Text(
                text = props.title.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(4f)
            )

            props.moreLink?.let {
                Box(
                    modifier = Modifier
                        .clickable { navController.navigate(it) }
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = props.moreLinkText ?: "See all",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val spacing = 8.dp
            val totalSpacing = spacing * (visibleCards - 1)
            val cardWidth = (maxWidth - totalSpacing) / (visibleCards + peekFraction)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentPadding = PaddingValues(start = spacing * 2, end = spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                items(props.items, key = { it.id }) { item ->
                    val aspectRatio = aspectFromComponent(item.component)

                    NMComponent(
                        components = listOf(item),
                        navController = navController,
                        aspectRatio = aspectRatio,
                        modifier = Modifier
                            .width(cardWidth)
                            .aspectFromType(aspectRatio)
                    )
                }
            }
        }
    }
}

fun aspectFromComponent(componentName: String?): AspectRatio {
    return when (componentName) {
        "NMCard" -> AspectRatio.Poster
        "NMMusicCard" -> AspectRatio.Cover
        else -> AspectRatio.Poster
    }
}