package tv.nomercy.app.tv.layout

import AppNavItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.nomercy.app.R
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.auth.AuthViewModelFactory
import tv.nomercy.app.shared.components.MoooomIcon
import tv.nomercy.app.shared.components.nMComponents.CoverImage
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.tv.layout.app_icon.IcLauncherForeground

@Composable
fun TvNavigationBar(
    navController: NavHostController,
    navItems: List<AppNavItem>,
    modifier: Modifier = Modifier
) {

    val musicStore = GlobalStores.getMusicPlayerStore(LocalContext.current)
    val currentSong by musicStore.currentSong.collectAsState()
    val isPlaying by musicStore.isPlaying.collectAsState()

    Row(
        modifier = modifier
            .background(Color.Transparent)
            .padding(horizontal = 56.dp)
            .fillMaxWidth()
            .height(88.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        if (currentSong != null) {
            SongPreviewButton(
                song = currentSong!!,
                isPlaying = isPlaying,
                onClick = {

                }
            )
        } else {
            AppLogoSquare()
        }

        Row(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxWidth()
                .weight(3f)
                .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                navItems.forEach { item ->
                    TvNavigationBarButton(item, navController)
                }
            }

        }
        ProfileImage()
    }
}

@Composable
fun SongPreviewButton(
    song: PlaylistItem,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(240.dp)
            .height(40.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.size(40.dp)) {

            CoverImage(cover = song.cover, name = song.name, modifier = Modifier.matchParentSize().graphicsLayer { alpha = 0.5f })

            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                if (isPlaying) Text("▶", color = Color.White) else Text("▷", color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MarqueeText(song.name)
            Text(
                text = song.artistTrack.joinToString(", ") { it.name },
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TvNavigationBarButton(
    item: AppNavItem,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

    val inactiveBg = Color.Black.copy(alpha = 0.6f)
    val activeBrush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)))

    Box(
        modifier = modifier
            .then(

                if (isSelected) Modifier
                    .background(activeBrush, shape = RoundedCornerShape(20.dp))
                    .shadow(6.dp, shape = RoundedCornerShape(20.dp))

                else Modifier
                    .background(inactiveBg, shape = RoundedCornerShape(20.dp))
            )
            .focusable()
            .clickable {
                navController.navigate(item.route)
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
        ) {
            MoooomIcon(
                icon = item.icon,
                contentDescription = stringResource(item.name),
                modifier = Modifier
                    .size(20.dp),
                tint = Color.White
            )

            Text(
                text = stringResource(item.name),
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProfileImage(size: Dp = 44.dp) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current))

    val userInfo by authViewModel.userInfo.collectAsState()

    Box(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .size(size)
    ) {
        if (!userInfo?.avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userInfo?.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = userInfo?.username,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, Color.Transparent, shape = CircleShape),
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.user),
                error = painterResource(R.drawable.user)
            )
        } else {
            Box(modifier = Modifier.size(size).background(Color.DarkGray, shape = CircleShape))
        }

        Box(
            modifier = Modifier
                .size(10.dp)
                .offset(x = 4.dp, y = (-4).dp)
                .align(Alignment.TopEnd)
                .background(Color(0xFF3DDC84), shape = CircleShape)
                .border(2.dp, Color.White, shape = CircleShape)
        )
    }
}

@Composable
fun AppLogoSquare(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .padding(bottom = 12.dp)
    ) {
        Image(
            imageVector = IcLauncherForeground,
            contentDescription = "")
    }
}

@Composable fun MarqueeText(text: String) { Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
