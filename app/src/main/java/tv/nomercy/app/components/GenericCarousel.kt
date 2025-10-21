package tv.nomercy.app.components

import android.view.KeyEvent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import tv.nomercy.app.components.TMDBImage
import tv.nomercy.app.components.nMComponents.CompletionOverlay
import tv.nomercy.app.components.nMComponents.OverlayProps
import tv.nomercy.app.shared.models.ColorPalettes
import tv.nomercy.app.shared.models.Episode
import tv.nomercy.app.shared.models.Image
import tv.nomercy.app.shared.models.PaletteColors
import tv.nomercy.app.shared.models.Person
import tv.nomercy.app.shared.models.Related
import tv.nomercy.app.shared.ui.LocalCurrentItemFocusRequester
import tv.nomercy.app.shared.ui.LocalFocusLeftInRow
import tv.nomercy.app.shared.ui.LocalFocusRightInRow
import tv.nomercy.app.shared.ui.LocalOnActiveCardChange
import tv.nomercy.app.shared.ui.LocalOnActiveInRow
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.isTv
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor

interface CarouselItem {
    val id: Long
    val imagePath: String?
    val title: String?
    val link: String?
    val aspectRatio: AspectRatio
    val colorPalette: ColorPalettes?
    val mediaType: String?
    val numberOfItems: Int? get() = null
    val haveItems: Int? get() = null
    val progress: Int? get() = null
}

data class CarouselData(
    override val id: Long,
    override val imagePath: String?,
    override val title: String?,
    override val link: String?,
    override val aspectRatio: AspectRatio,
    override val colorPalette: ColorPalettes? = null,
    override val mediaType: String? = null,
    override val numberOfItems: Int? = null,
    override val haveItems: Int? = null,
    override val progress: Int? = null,
) : CarouselItem

fun Person.toCarouselItem() = CarouselData(
    id = id,
    imagePath = profile,
    title = name,
    link = link,
    aspectRatio = AspectRatio.Poster,
    mediaType = "person",
    colorPalette = colorPalette
)

fun Related.toCarouselItem() = CarouselData(
    id = id,
    imagePath = poster,
    title = title,
    link = link,
    aspectRatio = AspectRatio.Poster,
    mediaType = mediaType,
    colorPalette = colorPalette,
    numberOfItems = numberOfItems,
    haveItems = haveItems
)

fun Image.toCarouselItem() = CarouselData(
    id = id,
    imagePath = src,
    title = null,
    link = null,
    aspectRatio = if(type == "poster") AspectRatio.Poster else AspectRatio.Backdrop,
    mediaType = type,
    colorPalette = colorPalette
)

fun Episode.toCarouselItem() = CarouselData(
    id = id.toLong(),
    imagePath = still,
    title = title,
    link = link,
    aspectRatio = AspectRatio.Backdrop,
    mediaType = "episode",
    colorPalette = colorPalette,
    progress = progress
)

@Composable
fun GenericCarousel(
    title: String,
    items: List<CarouselItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    moreLink: String? = null,
    moreLinkText: String? = null,
    infoBanner: String? = "overlay",
    itemContent: @Composable (CarouselItem, cardWidth: Dp) -> Unit = { item, width ->

        val index = items.indexOf(item) + 1

        fun CarouselItem.paletteForType(): PaletteColors? = when (mediaType) {
            "poster", "backdrop", "logo", "image" -> colorPalette?.image
            "still" -> colorPalette?.still
            "tv", "movie" -> colorPalette?.poster
            else -> colorPalette?.profile ?: colorPalette?.still
        }

        val palette = item.paletteForType()
        val primary = MaterialTheme.colorScheme.primary
        val focusColor = remember(palette) { pickPaletteColor(palette, fallbackColor = primary) }

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
        val isTvPlatform = isTv()
        val isActive = isTvPlatform && (isFocused || isHovered)
        val borderWidth = animateDpAsState(
            targetValue = if (isActive) 2.dp else 1.dp,
            animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
            label = "nmcard-border"
        ).value
        val scale = animateFloatAsState(
            targetValue = if (isActive) 1.015f else 1.0f,
            animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
            label = "nmcard-scale"
        ).value

        val itemFocusRequester = LocalCurrentItemFocusRequester.current

        if (infoBanner == "underlay") {
            Box(
                modifier = modifier
                    .width(width)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, focusColor, RoundedCornerShape(6.dp))
                    .graphicsLayer { if (isTvPlatform) { scaleX = scale; scaleY = scale } }
                    .then(if (itemFocusRequester != null) Modifier.focusRequester(itemFocusRequester) else Modifier)
                    .then(
                        if (isTvPlatform) Modifier
                            .border(borderWidth, focusColor.copy(alpha = if (isActive) 1f else 0.5f), RoundedCornerShape(6.dp))
                            .focusable(interactionSource = interaction)
                            .hoverable(interactionSource = interaction)
                            .semantics { role = Role.Button }
                        else Modifier
                            .border(borderWidth, focusColor, RoundedCornerShape(6.dp))
                    )
                    .clickable { item.link?.let { navController.navigate(it) } },
                contentAlignment = Alignment.TopStart,
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopStart,
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectFromType(item.aspectRatio)
                                .paletteBackground(palette)
                        ) {
                            TMDBImage(
                                path = item.imagePath,
                                title = item.title,
                                aspectRatio = item.aspectRatio,
                                size = 300,
                                modifier = Modifier
                                    .fillMaxSize()
                            )

                            CompletionOverlay(
                                data = OverlayProps(
                                    numberOfItems = item.numberOfItems ?: 0,
                                    haveItems = item.haveItems ?: 0
                                ),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 0.dp, top = 16.dp)
                            )

                            FocusProgressBar(
                                progress = item.progress,
                                color = focusColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart)
                                    .height(4.dp)
                            )
                        }
                    }

                    item.title?.let { title ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.BottomStart,
                        ) {
                            Text(
                                text = "$index - $title",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.dp.value.sp),
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                minLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        else {
            Card(
                modifier = modifier
                    .width(width)
                    .aspectFromType(item.aspectRatio),
                border = BorderStroke(1.dp, focusColor),
                shape = RoundedCornerShape(6.dp),
                onClick = {
                    item.link?.let { navController.navigate(it) }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .paletteBackground(palette)
                ) {
                    TMDBImage(
                        path = item.imagePath,
                        title = item.title,
                        aspectRatio = item.aspectRatio,
                        size = 300,
                        modifier = Modifier
                            .clickable { item.link?.let { navController.navigate(it) } }
                    )

                    CompletionOverlay(
                        data = OverlayProps(
                            numberOfItems = item.numberOfItems ?: 0,
                            haveItems = item.haveItems ?: 0
                        ),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 0.dp, top = 16.dp)
                    )

                    item.title?.let { title ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                                .align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.dp.value.sp),
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                minLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    },
    headerContent: @Composable () -> Unit = {},
    visibleCards: Int = 3,
) {
    if (!items.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (title.isEmpty()) {
                headerContent()
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTv()) 36.dp else 52.dp)
                        .padding(
                            start = if (isTv()) 40.dp else 16.dp,
                            end = 16.dp,
                            top = if (isTv()) 4.dp else 12.dp,
                            bottom = 4.dp
                        )
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(4f)
                    )

                    moreLink?.let {
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
                                text = moreLinkText ?: "See all",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            BoxWithConstraints(
                modifier = modifier.fillMaxWidth()
            ) {
                val spacing = 8.dp
                val totalSpacing = spacing * (visibleCards - 1)

                val peekFraction = when (visibleCards) {
                    2 -> 0.25f // backdrop carousel
                    3 -> 0.4f  // poster carousel
                    else -> 0.3f // fallback
                }

                val cardWidth = (maxWidth - totalSpacing) / (visibleCards + peekFraction)
                val endPadding = spacing + cardWidth * peekFraction - 48.dp

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    contentPadding = PaddingValues(
                        start = if (isTv()) 40.dp else (spacing * 2),
                        end = if (endPadding < 0.dp) 18.dp else endPadding
                    ),
                ) {
                    items(items, key = { it.id }) { item ->
                        itemContent(item, cardWidth)
                    }
                }
            }
        }
    }
}

@Composable
fun FocusProgressBar(
    progress: Int?,
    color: Color,
    modifier: Modifier = Modifier
) {
    progress?.let {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(0.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = it / 100f)
                    .background(color, RoundedCornerShape(0.dp))
            )
        }
    }
}