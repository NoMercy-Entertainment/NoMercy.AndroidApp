package tv.nomercy.app.shared.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tv.nomercy.app.views.base.search.mobile.SearchScreen
import tv.nomercy.app.views.base.home.mobile.MobileHomeScreen
import tv.nomercy.app.views.base.info.mobile.InfoScreen
import tv.nomercy.app.views.base.libraries.mobile.LibrariesScreen
import tv.nomercy.app.views.base.library.mobile.LibraryScreen
import tv.nomercy.app.views.base.paginatedLibrary.mobile.PaginatedLibraryScreen
import tv.nomercy.app.views.base.person.mobile.PersonScreen
import tv.nomercy.app.views.base.watch.tv.WatchScreen
import tv.nomercy.app.views.profile.AboutScreen
import tv.nomercy.app.views.profile.AppSettingsScreen
import tv.nomercy.app.views.profile.ProfileScreen
import tv.nomercy.app.views.profile.ServerInfoScreen
import tv.nomercy.app.views.music.cards.mobile.CardsScreen
import tv.nomercy.app.views.music.genres.mobile.MusicGenreScreen
import tv.nomercy.app.views.music.list.mobile.ListScreen
import tv.nomercy.app.views.music.start.mobile.MusicStartScreen
import tv.nomercy.app.views.setup.selectServer.mobile.ServerSelectionScreen

@Composable
fun MobileNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "/home", modifier = modifier) {

        composable("/home") { MobileHomeScreen(navController) }
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

//                // 🛠 Setup
//                composable("/setup/select-servers") { SetupSelectServerScreen(navController) }
//                composable("/setup/no-servers") { SetupNoServerScreen(navController) }
//                composable("/setup/server-offline") { SetupServerOfflineScreen(navController) }
//                composable("/setup/post-install") { SetupPostInstallScreen(navController) }
//
//                // ⚙️ Preferences
//                composable("/preferences/display") { PreferencesDisplayScreen(navController) }
//                composable("/preferences/profile") { PreferencesProfileScreen(navController) }
//                composable("/preferences/controls") { PreferencesControlsScreen(navController) }
//                composable("/preferences/subtitles") { PreferencesSubtitlesScreen(navController) }
//
//                // 🧭 Dashboard (shared edit/detail screens)
//                composable("/dashboard/system") { DashboardSystemScreen(navController) }
//                composable("/dashboard/general") { DashboardGeneralScreen(navController) }
//                composable("/dashboard/users") { DashboardUsersScreen(navController) }
//                composable("/dashboard/users/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    EditUserScreen(id)
//                }
//                composable("/dashboard/libraries") { DashboardLibrariesScreen(navController) }
//                composable("/dashboard/libraries/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    EditLibraryScreen(id)
//                }
//                composable("/dashboard/specials") { DashboardSpecialsScreen(navController) }
//                composable("/dashboard/specials/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    EditSpecialScreen(id)
//                }
//                composable("/dashboard/devices") { DashboardDevicesScreen(navController) }
//                composable("/dashboard/devices/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    DeviceScreen(id)
//                }
//                composable("/dashboard/ripper") { DashboardRipperScreen(navController) }
//                composable("/dashboard/encoderprofiles") { DashboardEncoderProfilesScreen(navController) }
//                composable("/dashboard/encoderprofiles/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    EditEncoderProfileScreen(id)
//                }
//                composable("/dashboard/notifications") { DashboardNotificationsScreen(navController) }
//                composable("/dashboard/notifications/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    NotificationScreen(id)
//                }
//                composable("/dashboard/metadata") { DashboardMetadataScreen(navController) }
//                composable("/dashboard/activity") { DashboardActivityScreen(navController) }
//                composable("/dashboard/dlna") { DashboardDlnaScreen(navController) }
//                composable("/dashboard/logs") { DashboardLogsScreen(navController) }
//                composable("/dashboard/plugins") { DashboardPluginsScreen(navController) }
//                composable("/dashboard/plugins/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    PluginScreen(id)
//                }
//                composable("/dashboard/schedule") { DashboardScheduleScreen(navController) }
//                composable("/dashboard/schedule/{id}") { backStackEntry ->
//                    val id = backStackEntry.arguments?.getString("id")
//                    ScheduledTaskScreen(id)
//                }

        // 🚫 Fallback
        composable("/notfound") { NotFoundScreen(message = "Page not found", status = 404) }
    }
}

@Composable
fun NotFoundScreen(message: String, status: Int) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "$status",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
