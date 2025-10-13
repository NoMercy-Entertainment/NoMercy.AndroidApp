package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.nomercy.app.shared.components.TMDBImage
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMCardProps
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.getColorFromPercent
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun NMCard(
    component: Component,
    modifier: Modifier,
    navController: NavController,
    aspectRatio: AspectRatio? = null,
) {
    val wrapper = component.props as? NMCardWrapper ?: return
    val data = wrapper.data ?: return

    val focusColor: Color = remember(data.colorPalette) {
        val palette = data.colorPalette?.poster
        val color = pickPaletteColor(palette)
        color
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .aspectFromType(aspectRatio),
        border = BorderStroke(2.dp, focusColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(6.dp),
        onClick = {
            data.link.let { navController.navigate(it) }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .paletteBackground(data.colorPalette?.poster)) {
            TMDBImage(
                path = data.poster,
                title = data.title,
                aspectRatio = AspectRatio.Poster,
                size = 180,
            )

            CompletionOverlay(
                data = {
                    OverlayProps(
                        numberOfItems = data.numberOfItems,
                        haveItems = data.haveItems,
                        type = data.type
                    )
                }(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 0.dp, top = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.dp.value.sp),
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

@Serializable
data class OverlayProps(
    @SerialName("number_of_items")
    val numberOfItems: Int? = 0,
    @SerialName("have_items")
    val haveItems: Int? = 0,
    val type: String = ""
)


@Composable
fun CompletionOverlay(
    data: OverlayProps,
    modifier: Modifier = Modifier
) {

    val percent = calculateCompletionPercent(data.haveItems, data.numberOfItems)
    val color = getColorFromPercent(percent)
    val collapsed = shouldCollapsePill(data)

    val value = remember(data.haveItems, data.numberOfItems) {
        if (data.haveItems == null || data.numberOfItems == null || (data.haveItems == 0 && data.numberOfItems == 0)) null
        else "${data.haveItems} of ${data.numberOfItems}"
    }

    if (value != null) {
        Box(
            modifier = modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        topEnd = 6.dp,
                        bottomEnd = 6.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(
                        topEnd = 6.dp,
                        bottomEnd = 6.dp
                    )
                )
                .padding(
                    horizontal = if (collapsed) 0.dp else 6.dp,
                    vertical = 1.dp
                )
                .defaultMinSize(
                    minWidth = 8.dp,
                    minHeight = 16.dp
                )
        ) {
            if (!collapsed) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if(percent > 30 && percent < 80) Color.Black else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun calculateCompletionPercent(haveItems: Int?, numberOfItems: Int?): Int {
    if (haveItems == null || numberOfItems == null || numberOfItems == 0) return 0
    return ((haveItems.toFloat() / numberOfItems) * 100).toInt().coerceIn(0, 100)
}

fun shouldCollapsePill(data: OverlayProps): Boolean {
    return data.numberOfItems == 1 &&
            (data.haveItems == 0 || data.haveItems == 1)
}