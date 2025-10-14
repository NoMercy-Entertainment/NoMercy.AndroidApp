package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMMusicHomeCardProps
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun NMMusicHomeCard(
    component: Component,
    modifier: Modifier,
    navController: NavController,
    aspectRatio: AspectRatio? = null,
) {
    val wrapper = component.props as? NMMusicHomeCardProps ?: return
    val data = wrapper.data ?: return

    val serverConfigStore = GlobalStores.getServerConfigStore(LocalContext.current)
    serverConfigStore.currentServer.collectAsState()

    val ringColor = remember(data.colorPalette?.cover) {
        pickPaletteColor(data.colorPalette?.cover)
    }

    val backgroundGradient = if (data.type != "playlists") {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF021C37).copy(alpha = 0.08f),
                Color(0xFF05294D).copy(alpha = 0.03f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                ringColor.copy(alpha = 0.20f),
                ringColor.copy(alpha = 0.15f),
                ringColor.copy(alpha = 0.10f),
                ringColor.copy(alpha = 0.05f),
            )
        )
    }

    val labelText = "Most listened ${data.type?.removeSuffix("s")?.replaceFirstChar { it.uppercase() }}"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = backgroundGradient
            )
            .clickable { data.link?.let { navController.navigate(it) } }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image container
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDfeffe).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                MusicCardImage(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectFromType(AspectRatio.Cover))
            }

            // Text content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = data.name.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}