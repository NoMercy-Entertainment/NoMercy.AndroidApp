package tv.nomercy.app.ui.shared

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.ui.phone.AuthScreen
import tv.nomercy.app.ui.phone.MobileMainScreen

@Composable
fun RootNav() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(onAuthSuccess = {
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }
        composable("main") { MobileMainScreen() }
    }
}
