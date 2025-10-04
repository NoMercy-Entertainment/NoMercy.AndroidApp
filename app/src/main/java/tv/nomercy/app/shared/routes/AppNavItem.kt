import tv.nomercy.app.R

sealed class AppNavItem(
    val route: String,
    val title: String,
    val icon: Int,
    val description: String,
    val isMobileVisible: Boolean = true,
    val isTvVisible: Boolean = true
) {
    object Home : AppNavItem("home", "Home", R.drawable.home1, "Main dashboard")
    object Libraries : AppNavItem("libraries", "Libraries", R.drawable.folder, "Content organization")
    object Search : AppNavItem("search", "Search", R.drawable.searchmagnifyingglass, "Global search")
    object Music : AppNavItem("music", "Music", R.drawable.noteeighthpair, "Music playback")
    object Profile : AppNavItem("profile", "Profile", R.drawable.user, "User settings")
    object ServerSelection : AppNavItem("server_selection", "Server", R.drawable.server, "Select server", isTvVisible = false)
    object ServerInfo : AppNavItem("server_info", "Info", R.drawable.info, "Server info", isTvVisible = false)
    object About : AppNavItem("about", "About", R.drawable.info, "About app", isTvVisible = false)
}