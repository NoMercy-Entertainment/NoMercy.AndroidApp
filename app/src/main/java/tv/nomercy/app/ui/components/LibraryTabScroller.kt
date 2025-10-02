package tv.nomercy.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import tv.nomercy.app.R
import tv.nomercy.app.ui.phone.LibrariesViewModel

@Composable
fun LibraryTabScroller(viewModel: LibrariesViewModel, cb: (selectedTab: TabItem?) -> Unit?) {

    val libraries by viewModel.libraries.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf<TabItem?>(null) }

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
                        viewModel.loadLibrary(library.link)
                    }))

                "movie" -> add(TabItem(
                    library.id,
                    library.title,
                    library.type,
                    library.link,
                    R.drawable.filmmedia,
                    onClick = {
                        selectedTabIndex = libraries.indexOf(library)
                        viewModel.loadLibrary(library.link)
                    }))

                "tv" -> add(TabItem(
                    library.id,
                    library.title,
                    library.type,
                    library.link,
                    R.drawable.tv,
                    onClick = {
                        selectedTabIndex = libraries.indexOf(library)
                        viewModel.loadLibrary(library.link)
                    }))

                //"music" -> add(TabItem(
                //    library.id,
                //    library.title,
                //    library.type,
                //    "music/home",
                //    R.drawable.noteeighthpair,
                //    onClick = {
                //        selectedTabIndex = libraries.indexOf(library)
                //        viewModel.loadLibrary("music/home")
                //    }))
            }
        }

        // Add static tabs
        add(TabItem(
            "collections",
            "Collections",
            "collections",
            "/collections",
            R.drawable.collection1,
            onClick = {
                selectedTabIndex = libraries.size - 1;
                viewModel.loadLibrary("/collections")
            }))
        add(TabItem(
            "specials",
            "Specials",
            "specials",
            "/specials",
            R.drawable.sparkles,
            onClick = {
                selectedTabIndex = libraries.size;
                viewModel.loadLibrary("/specials")
            }))
        add(TabItem(
            "genres",
            "Genres",
            "genres",
            "/genres",
            R.drawable.witchhat,
            onClick = {
                selectedTabIndex = libraries.size + 1;
                viewModel.loadLibrary("/genres")
            }))
        add(TabItem(
            "people",
            "People",
            "people",
            "/people",
            R.drawable.user,
            onClick = {
                selectedTabIndex = libraries.size + 2;
                viewModel.loadLibrary("/people")
            }))
    }

    if (libraries.isNotEmpty()) {
        ScrollablePillList(tabs, selectedTabIndex)
    }

    selectedTab = tabs[selectedTabIndex]

    cb(selectedTab)
}