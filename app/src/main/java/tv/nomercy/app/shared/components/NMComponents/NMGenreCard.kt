package tv.nomercy.app.shared.components.NMComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.R
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.MediaItem
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType


@Composable
fun <T> NMGenreCard(
    component: Component<T>,
    modifier: Modifier,
    navController: NavController,
    index: Int = 0
) {
    val data = component.props.data ?: return
    if (data !is MediaItem) {
        println("NMCard received unexpected data type: ${data::class.simpleName}")
        return
    }

    val style = genreStyle(data.title)

    val isHovered = false;

    val hoverRotation = if (isHovered) {
        if (index % 2 == 0) -4f else 4f
    } else 0f

    val hoverBorder = if (isHovered) {
        BorderStroke(
            width = 2.dp,
            color = style.iconBackground.copy(alpha = 0.5f))
    } else {
        BorderStroke(
            width = 1.dp,
            color = Color.Transparent
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectFromType(AspectRatio.Poster),
        border = hoverBorder,
        shape = RoundedCornerShape(6.dp),
        onClick = {
//            data.link.let { navController.navigate(it) }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(style.backgroundColor)
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.08f),
                )
                .clickable {
                    // navController.navigate(data.link)
                }
                .drawWithCache {
//                    val radial = Brush.radialGradient(
//                        colors = listOf(
//                            Color.Black.copy(alpha = 0.15f),
//                            Color.Transparent
//                        ),
//                        center = Offset(-size.width * 0.1f, -size.height * 0.1f),
//                        radius = size.minDimension * 1.4f
//                    )
//
//                    val linear = Brush.verticalGradient(
//                        colors = listOf(
//                            Color.Transparent,
//                            Color.Black.copy(alpha = 0.12f)
//                        )
//                    )
//
//                    val bottomFade = Brush.verticalGradient(
//                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f)),
//                        startY = size.height * 0.7f,
//                        endY = size.height
//                    )
//
                    onDrawBehind {
//                        drawRect(Color.Black.copy(alpha = 0.4f)) // base dark overlay
//                        drawRect(radial) // radial highlight
//                        drawRect(linear) // vertical glow
//                        drawRect(bottomFade)
//                        drawLine(
//                            color = Color.White.copy(alpha = 0.24f),
//                            start = Offset(0f, 0f),
//                            end = Offset(size.width, 0f),
//                            strokeWidth = 1f
//                        ) // top inset stroke
                    }
                },
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon section
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer {
                            rotationZ = hoverRotation
                            scaleX = 1.1f
                            scaleY = 1.1f
                        }
                        .background(
                            color = style.iconBackground.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.09f),
                                    Color.Transparent
                                ),
                                center = Offset(0.5f, 1f),
                                radius = 100f
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = Color.Black.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = style.iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Text section
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.
                    fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 4.dp,
                        bottom = 32.dp,
                    ),
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = style.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )

                Text(
                    text = "${data.haveItems} ${if (data.haveItems == 1) "item" else "items"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = style.textColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}


data class GenreStyle(
    val backgroundColor: Color,
    val iconRes: Int,
    val iconBackground: Color,
    val textColor: Color,
)
@Composable
fun genreStyle(title: String): GenreStyle {
    return when (title) {
        "Action & Adventure" -> GenreStyle(colorResource(id = R.color.green_12), R.drawable.lightning, colorResource(id = R.color.green_11), colorResource(id = R.color.green_5))
        "Action" -> GenreStyle(colorResource(id = R.color.orange_12), R.drawable.muscle, colorResource(id = R.color.orange_11), colorResource(id = R.color.orange_5))
        "Adventure" -> GenreStyle(colorResource(id = R.color.gold_12), R.drawable.mapclue, colorResource(id = R.color.gold_11), colorResource(id = R.color.gold_5))
        "Animation" -> GenreStyle(colorResource(id = R.color.cyan_12), R.drawable.designertools, colorResource(id = R.color.cyan_11), colorResource(id = R.color.cyan_5))
        "Comedy" -> GenreStyle(colorResource(id = R.color.purple_12), R.drawable.opensmile, colorResource(id = R.color.purple_11), colorResource(id = R.color.purple_5))
        "Crime" -> GenreStyle(colorResource(id = R.color.tomato_12), R.drawable.chefknife, colorResource(id = R.color.tomato_11), colorResource(id = R.color.tomato_5))
        "Documentary" -> GenreStyle(colorResource(id = R.color.gray_12), R.drawable.folderopen, colorResource(id = R.color.gray_11), colorResource(id = R.color.gray_5))
        "Drama" -> GenreStyle(colorResource(id = R.color.pink_12), R.drawable.sweatsmile, colorResource(id = R.color.pink_11), colorResource(id = R.color.pink_5))
        "Family" -> GenreStyle(colorResource(id = R.color.indigo_12), R.drawable.multiusers, colorResource(id = R.color.indigo_11), colorResource(id = R.color.indigo_5))
        "Fantasy" -> GenreStyle(colorResource(id = R.color.green_12), R.drawable.witchhat, colorResource(id = R.color.green_11), colorResource(id = R.color.green_5))
        "History" -> GenreStyle(colorResource(id = R.color.brown_12), R.drawable.paperscroll, colorResource(id = R.color.brown_11), colorResource(id = R.color.brown_5))
        "Horror" -> GenreStyle(colorResource(id = R.color.red_12), R.drawable.ghost, colorResource(id = R.color.red_11), colorResource(id = R.color.red_5))
        "Kids" -> GenreStyle(colorResource(id = R.color.blue_12), R.drawable.mouseanimals, colorResource(id = R.color.blue_11), colorResource(id = R.color.blue_5))
        "Music" -> GenreStyle(colorResource(id = R.color.amber_12), R.drawable.notesixteenthpair, colorResource(id = R.color.amber_11), colorResource(id = R.color.amber_5))
        "Mystery" -> GenreStyle(colorResource(id = R.color.teal_12), R.drawable.footprint, colorResource(id = R.color.teal_11), colorResource(id = R.color.teal_5))
        "News" -> GenreStyle(colorResource(id = R.color.red_12), R.drawable.globe, colorResource(id = R.color.red_11), colorResource(id = R.color.red_5))
        "Reality" -> GenreStyle(colorResource(id = R.color.sky_12), R.drawable.eye, colorResource(id = R.color.sky_11), colorResource(id = R.color.sky_5))
        "Romance" -> GenreStyle(colorResource(id = R.color.crimson_12), R.drawable.bubbles, colorResource(id = R.color.crimson_11), colorResource(id = R.color.crimson_5))
        "Sci-Fi & Fantasy" -> GenreStyle(colorResource(id = R.color.blue_12), R.drawable.ussenterprise, colorResource(id = R.color.blue_11), colorResource(id = R.color.blue_5))
        "Science Fiction" -> GenreStyle(colorResource(id = R.color.sky_12), R.drawable.rocket, colorResource(id = R.color.sky_11), colorResource(id = R.color.sky_5))
        "Soap" -> GenreStyle(colorResource(id = R.color.purple_12), R.drawable.bubbles, colorResource(id = R.color.purple_11), colorResource(id = R.color.purple_5))
        "Talk" -> GenreStyle(colorResource(id = R.color.slate_12), R.drawable.megaphone, colorResource(id = R.color.slate_11), colorResource(id = R.color.slate_5))
        "Thriller" -> GenreStyle(colorResource(id = R.color.mint_12), R.drawable.searchmagnifyingglass, colorResource(id = R.color.mint_11), colorResource(id = R.color.mint_5))
        "TV Movie" -> GenreStyle(colorResource(id = R.color.yellow_12), R.drawable.tv, colorResource(id = R.color.yellow_11), colorResource(id = R.color.yellow_5))
        "War & Politics" -> GenreStyle(colorResource(id = R.color.sand_12), R.drawable.medal, colorResource(id = R.color.sand_11), colorResource(id = R.color.sand_5))
        "War" -> GenreStyle(colorResource(id = R.color.green_12), R.drawable.gun, colorResource(id = R.color.green_11), colorResource(id = R.color.green_5))
        "Western" -> GenreStyle(colorResource(id = R.color.yellow_12), R.drawable.cowboyhat, colorResource(id = R.color.yellow_11), colorResource(id = R.color.yellow_5))
        else -> GenreStyle(colorResource(id = R.color.slate_12), R.drawable.bat, colorResource(id = R.color.gray_11), colorResource(id = R.color.gray_5))
    }
}
