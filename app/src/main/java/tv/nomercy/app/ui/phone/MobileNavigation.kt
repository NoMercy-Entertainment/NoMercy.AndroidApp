package tv.nomercy.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tv.nomercy.app.R
import tv.nomercy.app.auth.AuthService
import tv.nomercy.app.auth.AuthState
import tv.nomercy.app.auth.AuthViewModel
import tv.nomercy.app.auth.AuthViewModelFactory
import tv.nomercy.app.setup.SetupScreen
import tv.nomercy.app.setup.SetupViewModel
import tv.nomercy.app.setup.SetupViewModelFactory
import tv.nomercy.app.store.AppConfigStore
import tv.nomercy.app.store.GlobalStores


sealed class MobileNavItem(
    val route: String,
    val title: String,
    val icon: Int,
    val description: String
) {
    object Home : MobileNavItem(
        route = "home",
        title = "Home",
        icon = R.drawable.home1,
        description = "Main dashboard with content recommendations"
    )
    object Libraries : MobileNavItem(
        route = "libraries",
        title = "Libraries",
        icon = R.drawable.folder,
        description = "Movies, TV shows, and content organization"
    )
    object Search : MobileNavItem(
        route = "search",
        title = "Search",
        icon = R.drawable.searchmagnifyingglass,
        description = "Global search across all content types"
    )
    object Music : MobileNavItem(
        route = "music",
        title = "Music",
        icon = R.drawable.noteeighthpair,
        description = "Music library and audio playback"
    )
    object Profile : MobileNavItem(
        route = "profile",
        title = "Profile",
        icon = R.drawable.user,
        description = "User settings and preferences"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileMainScreen() {
    val context = LocalContext.current

    // Use singleton global stores - ensures same instances everywhere!
    val authStore = GlobalStores.getAuthStore(context)
    val authService = GlobalStores.getAuthService(context)
    val appConfigStore = GlobalStores.getAppConfigStore(context)

    // Pass the singleton instances to ensure all components use the same stores
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context, authStore))
    val setupViewModel: SetupViewModel = viewModel(factory = SetupViewModelFactory(appConfigStore))

    val authState by authViewModel.authState.collectAsState()

    // State to track if setup is complete
    var isSetupComplete by remember { mutableStateOf(false) }

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Unauthenticated, is AuthState.Error -> {
            // Reset setup when user logs out
            LaunchedEffect(authState) {
                if (authState is AuthState.Unauthenticated) {
                    isSetupComplete = false
                    appConfigStore.clearData()
                }
            }

            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Navigation will automatically update when authState changes
                }
            )
        }
        is AuthState.Authenticated -> {
            if (!isSetupComplete) {
                // Show setup flow after authentication
                LaunchedEffect(Unit) {
                    setupViewModel.checkSetupRequirements()
                }

                SetupScreen(
                    setupViewModel = setupViewModel,
                    onSetupComplete = {
                        isSetupComplete = true
                    }
                )
            } else {
                // Show main app after setup is complete
                AuthenticatedMainScreen(authViewModel = authViewModel, appConfigStore = appConfigStore)
            }
        }
        AuthState.AwaitingLogin -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Signing you in...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedMainScreen(authViewModel: AuthViewModel, appConfigStore: AppConfigStore) {
    val navController = rememberNavController()
    val navItems = listOf(
        MobileNavItem.Home,
        MobileNavItem.Libraries,
        MobileNavItem.Search,
        MobileNavItem.Music,
        MobileNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NoMercyBottomNavigationBar(
                navController = navController,
                navItems = navItems,
                appConfigStore = appConfigStore,
                authViewModel = authViewModel
            )
        }
    ) { innerPadding ->
        MobileNavHost(
            navController = navController,
            authViewModel = authViewModel,
            appConfigStore = appConfigStore,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun NoMercyBottomNavigationBar(
    navController: NavHostController,
    navItems: List<MobileNavItem>,
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
                        .padding(vertical = 2.dp, horizontal = 6.dp)
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
                                .background(
                                    if (isSelected) Color(0xFF3D5AFE) else Color.Transparent
                                ),
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

@Composable
fun MobileNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    appConfigStore: AppConfigStore,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MobileNavItem.Home.route,
        modifier = modifier
    ) {
        composable(MobileNavItem.Home.route) {
            MobileHomeScreen()
        }
        composable(MobileNavItem.Libraries.route) {
            LibrariesScreen()
        }
        composable(MobileNavItem.Search.route) {
            SearchScreen()
        }
        composable(MobileNavItem.Music.route) {
            MusicScreen()
        }
        composable(MobileNavItem.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                appConfigStore = appConfigStore,
                onNavigateToServerSelection = {
                    navController.navigate("server_selection")
                },
                onNavigateToServerInfo = {
                    navController.navigate("server_info")
                },
                onNavigateToAbout = {
                    navController.navigate("about")
                }
            )
        }
        composable("server_selection") {
            ServerSelectionScreen(
                appConfigStore = appConfigStore,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onServerSelected = { server ->
                    navController.popBackStack()
                }
            )
        }
        composable("server_info") {
            ServerInfoScreen(
                appConfigStore = appConfigStore,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("about") {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
