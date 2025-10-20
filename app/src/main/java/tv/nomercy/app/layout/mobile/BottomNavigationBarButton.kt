package tv.nomercy.app.layout.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.ProfileImage
import tv.nomercy.app.shared.utils.gradientButtonBackground

@Composable
fun BottomNavigationBarButton(
    item: AppNavItem,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 4.dp, start = 4.dp, bottom = 0.dp, end = 4.dp)
            .clickable {
                navController.navigate(item.route)
            }
    ) {
        if (item.route == "/profile") {
            ProfileImage(
                size = 28.dp,
                bottomPadding = 0.dp,
                showPresenceDot = false,
                borderWidth = if (isSelected) 2.dp else 0.dp,
                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.then(if (isSelected) Modifier.rotate(12f) else Modifier)
            )
        }
        else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .then(if (isSelected) gradientButtonBackground() else Modifier),
                contentAlignment = Alignment.Center
            ) {
                MoooomIcon(
                    icon = item.icon,
                    contentDescription = stringResource(item.name),
                    modifier = Modifier
                        .size(20.dp)
                        .then(
                            if (isSelected) Modifier.rotate(12f) else Modifier
                        ),
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(item.name),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}
