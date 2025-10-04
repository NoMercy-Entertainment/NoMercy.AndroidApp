package tv.nomercy.app.shared.components.NMComponents

import ComponentData
import HomeItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.R
import tv.nomercy.app.shared.components.TMDBImage
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun <T : ComponentData> NMHomeCard(
    modifier: Modifier = Modifier,
    component: Component<HomeItem>,
    navController: NavController
) {
    val posterPalette = component.props.data?.colorPalette?.backdrop
    val focusColor = remember(posterPalette) { pickPaletteColor(posterPalette) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2 / 3f),
        border = BorderStroke(2.dp, focusColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = {
            // navController.navigate(component.props.data?.link)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paletteBackground(posterPalette)
        ) {
            TMDBImage(
                path = component.props.data?.backdrop,
                title = component.props.data?.title ?: component.props.data?.name,
                aspectRatio = AspectRatio.Backdrop,
                size = 180,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, focusColor, RoundedCornerShape(12.dp))
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.05f),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = component.props.data?.title ?: component.props.data?.name ?: "Unknown Title",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (component.props.data?.tags?.isNotEmpty() ?: false) {
                        Text(
                            text = component.props.data.tags.take(4).joinToString(", ") { it.replaceFirstChar(Char::uppercaseChar) },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = { navController.navigate("${component.props.data?.link}/watch") },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.play),
                                contentDescription = "Search",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Play")
                        }

                        OutlinedButton(
                            onClick = { component.props.data?.link?.let { navController.navigate(it) } },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.info),
                                contentDescription = "Info",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Info")
                        }
                    }
                }
            }
        }
    }
}