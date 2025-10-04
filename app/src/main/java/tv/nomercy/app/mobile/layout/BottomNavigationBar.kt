package tv.nomercy.app.mobile.layout

import AppNavItem
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tv.nomercy.app.R
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.shared.stores.AppConfigStore
import tv.nomercy.app.shared.utils.gradientButtonBackground
import kotlin.collections.forEach

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    navItems: List<AppNavItem>,
    appConfigStore: AppConfigStore,
    authViewModel: AuthViewModel
) {

    Surface(
        color = Color(0xFF1B1B1B),
        tonalElevation = 12.dp,
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            navItems.forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .fillMaxSize()
                        .weight(1f)
                        .padding(top = 6.dp, start = 6.dp, bottom = 4.dp, end = 6.dp)
                ) {
                    if (item.route == "profile") {
                        // Get user info for fallback avatar
                        val userInfo by authViewModel.userInfo.collectAsState()
                        val userProfile by appConfigStore.userProfile.collectAsState()

                        val avatarUrl = userProfile?.avatarUrl?.takeIf { it.isNotBlank() }
                            ?: userInfo?.avatarUrl?.takeIf { it.isNotBlank() }
                            ?: userInfo?.email?.let { email ->
                                "https://www.gravatar.com/avatar/${email.hashCode()}?d=retro&s=64"
                            }

                        // Profile image with border when selected
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.description,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF3D5AFE) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .then(
                                    if (isSelected) Modifier.rotate(12f) else Modifier
                                ),
                            contentScale = ContentScale.Crop,
                            fallback = painterResource(R.drawable.user),
                            error = painterResource(R.drawable.user)
                        )
                    } else {
                        // Regular icon with background for other tabs
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .then(if (isSelected) gradientButtonBackground() else Modifier),
//                                .background(
//                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
//                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.description,
                                modifier = Modifier
                                    .size(20.dp)
                                    .then(
                                        if (isSelected) Modifier.rotate(12f) else Modifier
                                    ),
                                tint = if (isSelected) Color.White else Color(0xFF9E9E9E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF9E9E9E),
                        maxLines = 1
                    )
                }
            }
        }
    }
}