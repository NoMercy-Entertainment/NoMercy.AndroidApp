package tv.nomercy.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.R
import tv.nomercy.app.shared.models.Season
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.isTv

@Composable
fun SeasonCarousel(
    seasons: List<Season>,
    navController: NavController,
    modifier: Modifier = Modifier,
    visibleCards: Int = 3,
) {
    if (seasons.isEmpty()) return

    var selectedSeason by remember { mutableStateOf(seasons.find { it.seasonNumber == 1 } ?: seasons.first()) }
    var expanded by remember { mutableStateOf(false) }

    val toBeAnnouncedItem = CarouselData(
        id = -1L,
        imagePath = null,
        title = "To be announced",
        link = null,
        aspectRatio = AspectRatio.Backdrop,
        mediaType = "announcement",
        colorPalette = null
    )

    val episodeItems = if (selectedSeason.episodes.isEmpty()) {
        listOf(toBeAnnouncedItem)
    } else {
        selectedSeason.episodes.map { it.toCarouselItem() }
    }

    GenericCarousel(
        title = "",
        items = episodeItems,
        navController = navController,
        modifier = modifier.fillMaxWidth(),
        infoBanner = "underlay",
        visibleCards = visibleCards,
        headerContent = {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (isTv()) 40.dp else 16.dp,
                        end = 16.dp,
                        top = if (isTv()) 4.dp else 12.dp,
                        bottom = 16.dp
                    )
            ) {
                val dropdownWidth = maxWidth * (if(isTv()) 0.3f else 0.6f)
                val isDisabled = seasons.size <= 1

                Column(modifier = Modifier.width(dropdownWidth)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .background(
                                color = Color.Transparent, // Always transparent now
                                shape = RoundedCornerShape(4.dp)
                            )
                            .then(
                                if (!isDisabled) Modifier.clickable { expanded = true }
                                else Modifier
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${selectedSeason.seasonNumber} - ${selectedSeason.title}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDisabled) 0.5f else 1f),
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!isDisabled) {
                                Icon(
                                    painter = painterResource(R.drawable.chevrondown),
                                    contentDescription = "Expand",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (!isDisabled) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.width(dropdownWidth)
                        ) {
                            seasons.forEach { season ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${season.seasonNumber} - ${season.title}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(dropdownWidth)
                                        )
                                    },
                                    onClick = {
                                        selectedSeason = season
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
