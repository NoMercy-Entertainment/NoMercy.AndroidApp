package tv.nomercy.app.shared.components.nMComponents

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
import tv.nomercy.app.shared.models.ComponentData
import tv.nomercy.app.shared.models.NMCardProps
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun <T: ComponentData> NMHomeCard(
    component: Component<out T>,
    modifier: Modifier,
    navController: NavController,
    aspectRatio: AspectRatio? = null,
) {
    val data = component.props.data ?: return

    if (data !is NMCardProps) {
        println("NMCard received unexpected data type: ${data::class.simpleName}")
        return
    }

    val posterPalette = data.colorPalette?.backdrop
    val focusColor = remember(posterPalette) { pickPaletteColor(posterPalette) }


    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .aspectFromType(aspectRatio),
        border = BorderStroke(2.dp, focusColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(6.dp),
        onClick = {
//            data.link.let { navController.navigate(it) }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .paletteBackground(data.colorPalette?.poster)) {
            TMDBImage(
                path = data.poster,
                title = data.title,
                aspectRatio = AspectRatio.Poster,
                size = 500,
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
                        text = data.title ?: "Unknown Title",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

//                    if (data.tags?.isNotEmpty() ?: false) {
//                        Text(
//                            text = data.tags.take(4).joinToString(", ") { it.replaceFirstChar(Char::uppercaseChar) },
//                            style = MaterialTheme.typography.bodySmall,
//                            textAlign = TextAlign.Center
//                        )
//                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = { navController.navigate("${data.link}/watch") },
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
                            onClick = { data.link?.let { navController.navigate(it) } },
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