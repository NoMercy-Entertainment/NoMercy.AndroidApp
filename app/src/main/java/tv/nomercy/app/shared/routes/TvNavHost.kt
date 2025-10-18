package tv.nomercy.app.shared.routes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tv.nomercy.app.tv.screens.base.SearchScreen
import tv.nomercy.app.tv.screens.base.home.TvHomeScreen
import tv.nomercy.app.tv.screens.base.info.InfoScreen
import tv.nomercy.app.tv.screens.base.libraries.LibrariesScreen
import tv.nomercy.app.tv.screens.base.library.LibraryScreen
import tv.nomercy.app.tv.screens.base.paginatedLibrary.PaginatedLibraryScreen
import tv.nomercy.app.tv.screens.base.person.PersonScreen
import tv.nomercy.app.tv.screens.base.watch.WatchScreen
import tv.nomercy.app.tv.screens.dashboard.profile.AboutScreen
import tv.nomercy.app.tv.screens.dashboard.profile.AppSettingsScreen
import tv.nomercy.app.tv.screens.dashboard.profile.ProfileScreen
import tv.nomercy.app.tv.screens.dashboard.profile.ServerInfoScreen
import tv.nomercy.app.tv.screens.music.cards.CardsScreen
import tv.nomercy.app.tv.screens.music.genres.MusicGenreScreen
import tv.nomercy.app.tv.screens.music.list.ListScreen
import tv.nomercy.app.tv.screens.music.start.MusicStartScreen
import tv.nomercy.app.tv.screens.selectServer.ServerSelectionScreen

@Composable
fun TvNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "/home", modifier = modifier) {

        composable("/home") { TvHomeScreen(navController) }
        composable("/search") { SearchScreen(navController) }

        composable("/libraries") { LibrariesScreen(navController) }
        composable("/libraries/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            LibraryScreen(navController, id)
        }
        composable("/libraries/{id}/letter/{letter}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val letter = backStackEntry.arguments?.getString("letter")?.firstOrNull()
            LibraryScreen(navController, id, letter)
        }

        composable("/specials") { LibraryScreen(navController, "specials") }
        composable("/genre") { LibraryScreen(navController, "genre") }
        composable("/collection") { LibraryScreen(navController, "collection") }
        composable("/person") { PaginatedLibraryScreen("person") }

        composable("/genre/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            if (id == null) {
                NotFoundScreen(message = "Playlist not found", status = 404)
                return@composable
            }
            LibraryScreen(navController, "genre", id)
        }
        composable("/person/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            if (id == null) {
                NotFoundScreen(message = "Playlist not found", status = 404)
                return@composable
            }
            PersonScreen(id)
        }

        composable("/{type}/{id}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            val id = backStackEntry.arguments?.getString("id")
            if (id == null || type == null) {
                NotFoundScreen(message = "Playlist not found", status = 404)
                return@composable
            }
            InfoScreen(type, id, navController)
        }

        composable("/{type}/{id}/watch") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            val id = backStackEntry.arguments?.getString("id")
            if (id == null || type == null) {
                NotFoundScreen(message = "Playlist not found", status = 404)
                return@composable
            }
            WatchScreen(type, id, navController)
        }

        composable("/music/start") { MusicStartScreen(navController) }
        composable("/music/artists/{letter}") { backStackEntry ->
            val letter = backStackEntry.arguments?.getString("letter")?.firstOrNull()
            CardsScreen("artists", letter, navController)
        }

        composable("/music/genres") { CardsScreen("genres", null, navController) }
        composable("/music/genres/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            if (id == null) {
                NotFoundScreen(message = "Playlist not found", status = 404)
                return@composable
            }
            MusicGenreScreen("genres", id)
        }

        composable("/music/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            if (type == null) {
                CardsScreen("playlists", null, navController)
            } else {
                CardsScreen(type, null, navController)
            }
        }
        composable("/music/{type}/{id}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            val id = backStackEntry.arguments?.getString("id")
            if (id == null || type == null) {
                NotFoundScreen(message = "Playlist not found", status = 404)
                return@composable
            }
            ListScreen(type, id, navController)
        }

        // 👤 Profile & Info
        composable("/profile") {
            ProfileScreen(
                onNavigateToServerSelection = { navController.navigate("/serverSelection") },
                onNavigateToServerInfo = { navController.navigate("/serverInfo") },
                onNavigateToAbout = { navController.navigate("/about") },
                onNavigateToTheme = { navController.navigate("/profile/settings") }
            )
        }
        composable("/profile/settings") { AppSettingsScreen { navController.popBackStack() } }
        composable("/serverSelection") { ServerSelectionScreen { navController.popBackStack() } }
        composable("/serverInfo") { ServerInfoScreen { navController.popBackStack() } }
        composable("/about") { AboutScreen { navController.popBackStack() } }

        // 🚫 Fallback
        composable("/notfound") { NotFoundScreen(message = "Page not found", status = 404) }
    }
}