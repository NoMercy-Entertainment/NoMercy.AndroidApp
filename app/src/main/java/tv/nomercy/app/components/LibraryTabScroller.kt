package tv.nomercy.app.shared.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import tv.nomercy.app.R
import tv.nomercy.app.views.base.library.shared.LibrariesViewModel

@Composable
fun LibraryTabScroller(viewModel: LibrariesViewModel, currentLink: String? = null, navController: NavController, cb: ((selectedTab: TabItem?) -> Unit)? = null) {

    val libraries by viewModel.libraries.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    // Create tabs from libraries
    val tabs: List<TabItem> = buildList {
        libraries.forEach { library ->
            when (library.type) {

                "anime" -> add(TabItem(
                    library.id,
                    library.title,
                    library.type,
                    library.link,
                    R.drawable.tv,
                    onClick = {
                        selectedTabIndex = libraries.indexOf(library)
                        navController.navigate(library.link)
                    }))

                "movie" -> add(TabItem(
                    library.id,
                    library.title,
                    library.type,
                    library.link,
                    R.drawable.filmmedia,
                    onClick = {
                        selectedTabIndex = libraries.indexOf(library)
                        navController.navigate(library.link)
                    }))

                "tv" -> add(TabItem(
                    library.id,
                    library.title,
                    library.type,
                    library.link,
                    R.drawable.tv,
                    onClick = {
                        selectedTabIndex = libraries.indexOf(library)
                        navController.navigate(library.link)
                    }))
            }
        }

        // Add static tabs
        add(TabItem(
            "collections",
            "Collections",
            "collections",
            "/collection",
            R.drawable.collection1,
            onClick = {
                selectedTabIndex = libraries.size -1
                navController.navigate("/collection")
            }))
        add(TabItem(
            "specials",
            "Specials",
            "specials",
            "/specials",
            R.drawable.sparkles,
            onClick = {
                selectedTabIndex = libraries.size
                navController.navigate("/specials")
            }))
        add(TabItem(
            "genres",
            "Genres",
            "genres",
            "/genre",
            R.drawable.witchhat,
            onClick = {
                selectedTabIndex = libraries.size + 1
                navController.navigate("/genre")
            }))
//        add(TabItem(
//            "people",
//            "People",
//            "people",
//            "person",
//            R.drawable.user,
//            onClick = {
//                selectedTabIndex = libraries.size + 2;
//                viewModel.selectLibrary("person")
//            navController.navigate("/person")
//            }))
    }

    // Sync selectedTabIndex with currentLink
    LaunchedEffect(currentLink, tabs) {
        if (currentLink != null && tabs.isNotEmpty()) {
            // Try to find exact match first
            var index = tabs.indexOfFirst { it.link == currentLink }

            // If no exact match, try to find by matching the ID part
            if (index == -1) {
                index = tabs.indexOfFirst { tab ->
                    // Check if the tab link ends with the current link (e.g., "/libraries/1" ends with "1")
                    tab.link.endsWith("/$currentLink") ||
                    tab.link == "/libraries/$currentLink"
                }
            }

            if (index != -1 && index != selectedTabIndex) {
                selectedTabIndex = index
            }
        }
    }

    // Only call the callback when selectedTabIndex actually changes, and if callback is provided
    LaunchedEffect(selectedTabIndex, tabs) {
        if (tabs.isNotEmpty() && cb != null) {
            val selectedTab = tabs.getOrNull(selectedTabIndex)
            cb(selectedTab)
        }
    }

    if (libraries.isNotEmpty()) {
        ScrollablePillList(tabs, selectedTabIndex)
    }
}
