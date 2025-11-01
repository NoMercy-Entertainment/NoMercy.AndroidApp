package tv.nomercy.app.layout.mobile

import tv.nomercy.app.layout.mobile.AppNavItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    navItems: List<AppNavItem>,
    modifier: Modifier = Modifier,
) {

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 30.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            navItems.forEach { item ->
                BottomNavigationBarButton(item, navController, Modifier.weight(1f))
            }
        }
    }
}