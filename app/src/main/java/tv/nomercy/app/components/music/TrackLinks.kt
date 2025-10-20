package tv.nomercy.app.components.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.nomercy.app.components.Marquee
import tv.nomercy.app.shared.models.Album
import tv.nomercy.app.shared.models.Artist
import kotlin.collections.forEach

@Composable
fun TrackLinksAlbums(albums: List<Album>, navController: NavHostController) {
    albums.forEach { link ->
        val index = albums.indexOf(link)
        val text = link.name + if (albums.size > 1 && index < albums.size - 1) "," else ""
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .clickable {
                    navController.navigate(link.link)
                }
                .padding(end = 1.dp)
        )
    }
}

@Composable
fun TrackLinksArtists(artists: List<Artist>, navController: NavHostController) {
//    Marquee {
        artists.forEach { link ->
            val index = artists.indexOf(link)
            val text = link.name + if (artists.size > 1 && index < artists.size - 1) "," else ""
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .clickable {
                        navController.navigate(link.link)
                    }
                    .padding(end = 1.dp)
            )
        }
//    }
}
