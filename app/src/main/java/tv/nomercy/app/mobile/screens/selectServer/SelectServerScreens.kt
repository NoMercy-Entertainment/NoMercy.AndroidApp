package tv.nomercy.app.mobile.screens.selectServer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tv.nomercy.app.shared.models.Server

/**
 * Main setup screen that handles the setup flow
 */
@Composable
fun SelectServerScreen(
    selectServerViewModel: SelectServerViewModel,
    onSetupComplete: () -> Unit
) {
    val setupState by selectServerViewModel.setupState.collectAsState()
    val isLoading by selectServerViewModel.isLoading.collectAsState()

    LaunchedEffect(setupState) {
        if (setupState is SetupState.Complete) {
            onSetupComplete()
        }
    }

    // Extract to local variable to avoid smart cast issues
    val currentState = setupState

    when (currentState) {
        is SetupState.CheckingConfiguration -> {
            LoadingScreen()
        }
        is SetupState.NoServersAvailable -> {
            NoServersScreen(
                onRetry = { selectServerViewModel.retry() },
            )
        }
        is SetupState.ServerSelectionRequired -> {
            ServerSelectionScreen(
                servers = currentState.servers,
                onServerSelected = { server -> selectServerViewModel.selectServer(server) },
                isLoading = isLoading
            )
        }
        is SetupState.ServerOffline -> {
            ServerOfflineScreen(
                onRetry = { selectServerViewModel.retry() }
            )
        }
        is SetupState.Error -> {
            ErrorScreen(
                message = currentState.message,
                canRetry = currentState.canRetry,
                onRetry = { selectServerViewModel.retry() }
            )
        }
        is SetupState.Complete -> {
            // Will trigger onSetupComplete via LaunchedEffect
        }
    }
}

@Composable
private fun LoadingScreen() {
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
                text = "Setting up your app...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ServerSelectionScreen(
    servers: List<Server>,
    onServerSelected: (Server) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose Your Server",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Select which NoMercy server you'd like to connect to:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(servers) { server ->
                ServerCard(
                    server = server,
                    onSelect = { onServerSelected(server) },
                    enabled = !isLoading
                )
            }
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerCard(
    server: Server,
    onSelect: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        enabled = enabled
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (!server.description.isNullOrBlank()) {
                        Text(
                            text = server.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                server.status?.let { status ->
                    Surface(
                        color = when (status.lowercase()) {
                            "online", "active" -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = when (status.lowercase()) {
                                "online", "active" -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }
            }

            server.version?.let { version ->
                Text(
                    text = "Version: $version",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setup Failed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (canRetry) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try Again")
            }
        }
    }
}
