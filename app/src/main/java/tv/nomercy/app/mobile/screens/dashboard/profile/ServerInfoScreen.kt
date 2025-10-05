package tv.nomercy.app.mobile.screens.dashboard.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.nomercy.app.R
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.auth.AuthViewModelFactory
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.stores.AppConfigStore
import tv.nomercy.app.shared.stores.GlobalStores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerInfoScreen(
    onNavigateBack: () -> Unit = {}
) {
    val serverConfigStore = GlobalStores.getServerConfigStore(LocalContext.current)
    val currentServer by serverConfigStore.currentServer.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.arrowright), // You'll need a back arrow icon
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Server Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        currentServer?.let { server ->
            item {
                ServerSystemCard(server)
            }

            item {
                ServerHardwareCard()
            }

            item {
                ServerActionsCard(
                    onPause = { /* TODO: Implement pause */ },
                    onShutdown = { /* TODO: Implement shutdown */ }
                )
            }
        }
    }
}

@Composable
private fun ServerSystemCard(server: Server) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "System",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO: Implement pause */ },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.settings), // Pause icon
                            contentDescription = "Pause",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pause", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { /* TODO: Implement shutdown */ },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.doorout), // Shutdown icon
                            contentDescription = "Shutdown",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shutdown", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Information
            ServerInfoRow("Server", server.name)
            ServerInfoRow("Server version", server.version ?: "Unknown")
            ServerInfoRow("Uptime", "5 days 12 hours 25 minutes") // Mock data
            ServerInfoRow("OS", "Linux 5.15.0-143-Generic") // Mock data
            ServerInfoRow("Architecture", "X64") // Mock data
        }
    }
}

@Composable
private fun ServerHardwareCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hardware",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Hardware Information
            ServerInfoRow("CPU #1", "Intel(R) Xeon(R) CPU X5550 @") // Mock data
            ServerInfoRow("GPU #1", "08:03.0 VGA compatible controller:") // Mock data
            ServerInfoRow("Memory", "32GB DDR4") // Mock data
            ServerInfoRow("Storage", "2TB NVMe SSD") // Mock data
        }
    }
}

@Composable
private fun ServerActionsCard(
    onPause: () -> Unit,
    onShutdown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Server Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Use these actions carefully. They will affect server availability.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ServerInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
