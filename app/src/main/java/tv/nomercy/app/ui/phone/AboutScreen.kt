package tv.nomercy.app.ui.phone

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tv.nomercy.app.BuildConfig
import tv.nomercy.app.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.arrowleft),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "About",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            // App logo/icon
            Image(
                painter = painterResource(R.drawable.user), // Replace with your app icon
                contentDescription = "NoMercy App Icon",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        item {
            Text(
                text = "NoMercy MediaServer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "Android Client",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "App Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AboutInfoRow("Version", packageInfo?.versionName ?: "Unknown")
                    AboutInfoRow("Version Code", packageInfo?.versionCode?.toString() ?: "Unknown")
                    AboutInfoRow("Package Name", context.packageName)
                    AboutInfoRow("Build Type", if (BuildConfig.DEBUG) "Debug" else "Release")
                    AboutInfoRow("Target SDK", Build.VERSION.SDK_INT.toString())
                    AboutInfoRow("Compile SDK", packageInfo?.applicationInfo?.targetSdkVersion?.toString() ?: "Unknown")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Device Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AboutInfoRow("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
                    AboutInfoRow("Android Version", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    AboutInfoRow("Security Patch", Build.VERSION.SECURITY_PATCH)
                    AboutInfoRow("Build ID", Build.ID)
                    AboutInfoRow("Bootloader", Build.BOOTLOADER)
                    AboutInfoRow("Hardware", Build.HARDWARE)
                    AboutInfoRow("Board", Build.BOARD)
                    AboutInfoRow("CPU ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Runtime Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val runtime = Runtime.getRuntime()
                    val maxMemory = runtime.maxMemory() / 1024 / 1024
                    val totalMemory = runtime.totalMemory() / 1024 / 1024
                    val freeMemory = runtime.freeMemory() / 1024 / 1024
                    val usedMemory = totalMemory - freeMemory

                    AboutInfoRow("Max Memory", "${maxMemory}MB")
                    AboutInfoRow("Used Memory", "${usedMemory}MB")
                    AboutInfoRow("Free Memory", "${freeMemory}MB")
                    AboutInfoRow("Available Processors", runtime.availableProcessors().toString())
                    AboutInfoRow("Java VM", System.getProperty("java.vm.name") ?: "Unknown")
                    AboutInfoRow("Java Version", System.getProperty("java.version") ?: "Unknown")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Build Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AboutInfoRow("Build Time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(Build.TIME)))
                    AboutInfoRow("Build User", Build.USER)
                    AboutInfoRow("Build Host", Build.HOST)
                    AboutInfoRow("Build Tags", Build.TAGS)
                    AboutInfoRow("Build Type", Build.TYPE)
                    AboutInfoRow("Product", Build.PRODUCT)
                    AboutInfoRow("Brand", Build.BRAND)
                    AboutInfoRow("Radio Version", Build.getRadioVersion())
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "About NoMercy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "NoMercy MediaServer is a comprehensive media management and streaming solution. " +
                                "This Android client allows you to access your media library, manage servers, " +
                                "and enjoy your content on the go.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Features",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val features = listOf(
                        "🎬 Stream movies and TV shows",
                        "🎵 Music library management",
                        "🔍 Advanced search capabilities",
                        "👥 Multi-user support",
                        "🌐 Multiple server connections",
                        "📱 Mobile-optimized interface",
                        "🔒 Secure authentication"
                    )

                    features.forEach { feature ->
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Development Team",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Developed with ❤️ by the NoMercy Entertainment team",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "© 2024-2025 NoMercy Entertainment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutInfoRow(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
