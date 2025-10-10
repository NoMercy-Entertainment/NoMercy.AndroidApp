package tv.nomercy.app.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.nomercy.app.R
import tv.nomercy.app.shared.models.InfoResponse
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType

@Composable
fun InfoCard(
    infoData: InfoResponse?,
    navController: NavHostController,
    focusColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(top = 48.dp, start = 64.dp, end = 64.dp, bottom = 24.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (infoData == null) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .aspectFromType(AspectRatio.Poster)
            )
        } else {
            ImageBackdrop(
                infoData = infoData,
                focusColor = focusColor,
            )

            TopRow(
                infoData = infoData,
                navController = navController,
                modifier = Modifier.matchParentSize()
            )

            BottomRow(
                infoData = infoData,
                navController = navController,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
fun ImageBackdrop(
    infoData: InfoResponse?,
    focusColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .height(380.dp)
            .clip(RoundedCornerShape(12.dp))
            .aspectFromType(AspectRatio.Poster),
    ) {
        if (infoData?.poster != null) {
            TMDBImage(
                path = infoData.poster,
                title = infoData.title,
                size = 300,
                aspectRatio = AspectRatio.Poster,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectFromType(AspectRatio.Poster)
            )
        } else {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectFromType(AspectRatio.Poster)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        GradientBlurOverlay(
            baseColor = focusColor,
            modifier = Modifier
                .height(300.dp)
                .align(Alignment.BottomStart)
        )
    }
}


@Composable
fun TopRow(
    infoData: InfoResponse,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement
            .spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.Top
    ) {

        CircularIconButton(
            modifier = Modifier
                .padding(0.dp)
                .size(32.dp),
            iconRes = R.drawable.check,
            onClick = {

            },
        )

        CircularIconButton(
            modifier = Modifier
                .padding(0.dp)
                .size(32.dp),
            iconRes = R.drawable.heart,
            contentDescription = infoData.favorite.let { if (it) "Unfavorite" else "Favorite" },
            onClick = {

            },
        )

        CircularIconButton(
            modifier = Modifier
                .padding(0.dp)
                .size(32.dp),
            iconRes = R.drawable.sharesquare,
            contentDescription = "Share",
            onClick = {

            },
        )
    }
}

@Composable
fun BottomRow(
    infoData: InfoResponse,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement
            .spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {

        Button(
            onClick = { navController.navigate("${infoData.link}/watch") },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(50.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Watch",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Button(
            onClick = { /* do something */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            shape = RoundedCornerShape(50.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Watch trailer",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
