package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tv.nomercy.app.components.images.CoverImage
import tv.nomercy.app.components.music.StyledPlaybackButton
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMTopResultCardWrapper
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun NMTopResultCard(
    component: Component,
    modifier: Modifier,
    navController: NavHostController,
){
    val wrapper = component.props as? NMTopResultCardWrapper ?: return
    val data = wrapper.data ?: return

    val context = LocalContext.current

    val serverConfigStore = GlobalStores.getServerConfigStore(context)
    serverConfigStore.currentServer.collectAsState()

    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val focusColor = remember(data.colorPalette?.cover) {
        pickPaletteColor(data.colorPalette?.cover, fallbackColor = fallbackColor)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        focusColor.copy(alpha = 0.20f),
                        focusColor.copy(alpha = 0.15f),
                        focusColor.copy(alpha = 0.10f),
                        focusColor.copy(alpha = 0.05f),
                    )
                )
            )
    ) {
        CoverImage(
            cover = data.cover,
            name = data.title,
            modifier = Modifier
                .size(40.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = data.type,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                data.artists.firstOrNull()?.let {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                    )

                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        StyledPlaybackButton(
            modifier = Modifier.size(56.dp),
            backgroundColor = focusColor
        )

    }

}