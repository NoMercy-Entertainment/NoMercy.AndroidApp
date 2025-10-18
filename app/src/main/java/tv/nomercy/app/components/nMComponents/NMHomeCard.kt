package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import tv.nomercy.app.R
import tv.nomercy.app.shared.components.GradientBlurOverlay
import tv.nomercy.app.shared.components.TMDBImage
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMHomeCardWrapper
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor

@Composable
fun NMHomeCard(
    component: Component,
    modifier: Modifier,
    navController: NavController,
    aspectRatio: AspectRatio? = null,
) {
    val wrapper = component.props as? NMHomeCardWrapper ?: return
    val data = wrapper.data ?: return

    val posterPalette = data.colorPalette?.poster
    val focusColor = remember(posterPalette) { pickPaletteColor(posterPalette) }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 0.dp, start = 18.dp, end = 18.dp)
            .clip(RoundedCornerShape(12.dp))
            .aspectFromType(aspectRatio),
        border = BorderStroke(2.dp, focusColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .paletteBackground(data.colorPalette?.poster)
        ) {
            TMDBImage(
                path = data.poster,
                title = data.title,
                aspectRatio = AspectRatio.Poster,
                size = 500,
            )

            GradientBlurOverlay(
                baseColor = focusColor,
                maxHeight = 140.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(1f)
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {

                        Button(
                            onClick = { navController.navigate("${data.link}/watch") },
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.nmplaysolid),
                                contentDescription = "Search",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.watch),
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                        }


                        Button(
                            onClick = { data.link.let { navController.navigate(it) } },
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.infocircle),
                                contentDescription = "Search",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(Color.White),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.details),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}