package tv.nomercy.app.views.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.nomercy.app.R
import tv.nomercy.app.views.base.auth.shared.AuthViewModel
import tv.nomercy.app.views.base.auth.shared.AuthViewModelFactory
import tv.nomercy.app.shared.api.services.UserInfo
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.stores.AppConfigStore
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.stores.ServerConfigStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToServerSelection: () -> Unit = {},
    onNavigateToServerInfo: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {}
) {
    val serverConfigStore = GlobalStores.getServerConfigStore(LocalContext.current)
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current))
    val userInfo by authViewModel.userInfo.collectAsState()
    val currentServer by serverConfigStore.currentServer.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ProfileHeader(userInfo = userInfo)
        }

        currentServer?.let { server ->
            item {
                CurrentServerCard(
                    server = server,
                    onSwitchServer = onNavigateToServerSelection,
                    onServerInfo = onNavigateToServerInfo
                )
            }
        }

        item {
            QuickStatsCard()
        }

        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(settingsItems) { item ->
            SettingsItem(
                iconRes = item.iconRes,
                title = item.title,
                subtitle = item.subtitle,
                onClick = {
                    when (item.title) {
                        "Sign Out" -> authViewModel.logout()
                        "About" -> onNavigateToAbout()
                        "App Settings" -> onNavigateToTheme()
                        // Handle other settings items
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    userInfo: UserInfo?,
) {
    val appConfigStore = GlobalStores.getAppConfigStore(LocalContext.current)
    val userProfile by appConfigStore.userProfile.collectAsState()

    val avatarUrl = userProfile?.avatarUrl?.takeIf { it.isNotBlank() }
        ?: userInfo?.avatarUrl?.takeIf { it.isNotBlank() }
        ?: userInfo?.email?.let { email ->
            "https://www.gravatar.com/avatar/${email.hashCode()}?d=retro&s=80"
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.user),
                error = painterResource(R.drawable.user)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = userProfile?.name ?: userInfo?.username ?: "User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userInfo?.email ?: "No email",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentServerCard(
    server: Server,
    onSwitchServer: () -> Unit,
    onServerInfo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onServerInfo() }, // Make the entire card clickable to navigate to server info
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Server",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            painter = painterResource(R.drawable.arrowright),
                            contentDescription = "View server info",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }

                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (!server.description.isNullOrBlank()) {
                        Text(
                            text = server.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    server.status?.let { status ->
                        Surface(
                            color = when (status.lowercase()) {
                                "online", "active" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = when (status.lowercase()) {
                                    "online", "active" -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onError
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { onSwitchServer() },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "Switch",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }

            // Show permissions if available
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                server.isOwner?.let { isOwner ->
                    if (isOwner) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Owner",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                server.isManager?.let { isManager ->
                    if (isManager) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Manager",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }
            }

            server.version?.let { version ->
                Text(
                    text = "Version: $version",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = R.drawable.filmmedia,
                    value = "1,247",
                    label = "Movies"
                )
                StatItem(
                    icon = R.drawable.tv,
                    value = "342",
                    label = "TV Shows"
                )
                StatItem(
                    icon = R.drawable.noteeighthpair,
                    value = "89h",
                    label = "Music"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    @DrawableRes icon: Int,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsItem(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.arrowright),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class SettingsItemData(
    val iconRes: Int,
    val title: String,
    val subtitle: String? = null
)

private val settingsItems = listOf(
    SettingsItemData(
        iconRes = R.drawable.user,
        title = "Account Settings",
        subtitle = "Manage your account preferences"
    ),
    SettingsItemData(
        iconRes = R.drawable.settings,
        title = "App Settings",
        subtitle = "Configure app behavior"
    ),
    SettingsItemData(
        iconRes = R.drawable.download,
        title = "Downloads",
        subtitle = "Manage offline content"
    ),
    SettingsItemData(
        iconRes = R.drawable.notification,
        title = "Notifications",
        subtitle = "Control notification preferences"
    ),
    SettingsItemData(
        iconRes = R.drawable.messagebubblequestion,
        title = "Help & Support",
        subtitle = "Get help and contact support"
    ),
    SettingsItemData(
        iconRes = R.drawable.info,
        title = "About",
        subtitle = "App version and information"
    ),
    SettingsItemData(
        iconRes = R.drawable.doorout,
        title = "Sign Out",
        subtitle = "Sign out of your account"
    )
)
