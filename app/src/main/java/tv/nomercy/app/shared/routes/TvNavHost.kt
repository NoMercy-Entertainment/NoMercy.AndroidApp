package tv.nomercy.app.shared.routes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tv.nomercy.app.views.base.search.tv.SearchScreen
import tv.nomercy.app.views.base.home.tv.TvHomeScreen
import tv.nomercy.app.views.base.info.tv.InfoScreen
import tv.nomercy.app.views.base.libraries.tv.LibrariesScreen
import tv.nomercy.app.views.base.library.tv.LibraryScreen
import tv.nomercy.app.views.base.paginatedLibrary.tv.PaginatedLibraryScreen
import tv.nomercy.app.views.base.person.tv.PersonScreen
import tv.nomercy.app.views.base.watch.tv.WatchScreen
import tv.nomercy.app.views.dashboard.profile.tv.AboutScreen
import tv.nomercy.app.views.dashboard.profile.tv.AppSettingsScreen
import tv.nomercy.app.views.dashboard.profile.tv.ProfileScreen
import tv.nomercy.app.views.dashboard.profile.tv.ServerInfoScreen
import tv.nomercy.app.views.music.cards.tv.CardsScreen
import tv.nomercy.app.views.music.genres.tv.MusicGenreScreen
import tv.nomercy.app.views.music.list.tv.ListScreen
import tv.nomercy.app.views.music.start.tv.MusicStartScreen
import tv.nomercy.app.views.setup.selectServer.tv.ServerSelectionScreen

@Composable
fun TvNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "/music/album/83b72d02-1513-4a8b-af14-4d58acdf97e1", modifier = modifier) {

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