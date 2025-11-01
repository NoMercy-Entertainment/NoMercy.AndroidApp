package tv.nomercy.app.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.nomercy.app.shared.models.NMCardProps
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.models.NMMusicCardProps

@Composable
fun VideoMusicHeroSection(card: NMCardProps?, height: Dp) {
    HeroRow(
        title = card?.title,
        overview = card?.overview,
        maxLines = 5,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    )
}

@Composable
fun VideoMusicHeroSection(card: NMMusicCardProps?, height: Dp) {
    HeroRow(
        title = card?.name,
        overview = card?.description,
        maxLines = 5,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    )
}

@Composable
fun HeroRow(
    title: String?,
    overview: String?,
    modifier: Modifier = Modifier,
    maxLines: Int,
) {

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LeftColumn(title = title, overview = overview, maxLines = maxLines, modifier = Modifier.weight(3f))
        RightColumn(modifier = Modifier.weight(2f))
    }
}

@Composable
fun LeftColumn(
    title: String?,
    overview: String?,
    modifier: Modifier = Modifier,
    maxLines: Int?
) {
    val titleBlockHeight = 26.dp + 24.dp + 8.dp
    val overviewBlockHeight = 20.dp * (maxLines ?: 5)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 40.dp, end = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.height(titleBlockHeight).fillMaxWidth()) {
            Crossfade(targetState = title, animationSpec = tween(durationMillis = 200), label = "title-fade") { t ->
                if (!t.isNullOrBlank()) {
                    SplitTitleText(
                        title = t,
                        mainStyle = MaterialTheme.typography.headlineMedium
                            .copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                lineHeight = 26.sp
                            ),
                        subtitleStyle = MaterialTheme.typography.headlineSmall
                            .copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 24.sp
                            ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.height(overviewBlockHeight).fillMaxWidth()) {
            Crossfade(targetState = overview, animationSpec = tween(durationMillis = 200), label = "overview-fade") { o ->
                if (!o.isNullOrBlank()) {
                    Text(
                        text = o,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        ),
                        maxLines = maxLines ?: 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun RightColumn(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
    )
}
