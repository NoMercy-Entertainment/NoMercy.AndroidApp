package tv.nomercy.app.mobile.screens.dashboard.profile

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.stores.ColorScheme
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.ThemeName

@Composable
fun AppSettingsScreen(onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { DeviceNameSetting() }
            item { LanguageSetting() }
            item { ScreensaverDelaySetting() }
            item { ColorSchemeSetting() }
            item { ThemeColorsSetting() }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun DeviceNameSetting() {
    val appSettingsStore = GlobalStores.getAppSettingsStore(LocalContext.current)
    val deviceName by appSettingsStore.deviceName.collectAsState(initial = Build.MODEL)
    val coroutineScope = rememberCoroutineScope()

    Column {
        SectionTitle("Apparaat naam")
        OutlinedTextField(
            value = deviceName,
            onValueChange = { coroutineScope.launch { appSettingsStore.setDeviceName(it) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSetting() {
    val appSettingsStore = GlobalStores.getAppSettingsStore(LocalContext.current)
    val selectedLanguage by appSettingsStore.language.collectAsState(initial = "Nederlands")
    val coroutineScope = rememberCoroutineScope()
    val languages = listOf("Nederlands", "English")
    var expanded by remember { mutableStateOf(false) }

    Column {
        SectionTitle("Weergave taal")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLanguage,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            coroutineScope.launch { appSettingsStore.setLanguage(language) }
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreensaverDelaySetting() {
    val appSettingsStore = GlobalStores.getAppSettingsStore(LocalContext.current)
    val delay by appSettingsStore.screensaverDelay.collectAsState(initial = 5)
    val coroutineScope = rememberCoroutineScope()

    Column {
        SectionTitle("Schermbeveiliging vertraging")
        OutlinedTextField(
            value = delay.toString(),
            onValueChange = { coroutineScope.launch { appSettingsStore.setScreensaverDelay(it.toIntOrNull() ?: 0) } },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun ColorSchemeSetting() {
    val appSettingsStore = GlobalStores.getAppSettingsStore(LocalContext.current)
    val currentScheme by appSettingsStore.colorScheme.collectAsState(initial = ColorScheme.DARK)
    val coroutineScope = rememberCoroutineScope()

    Column {
        SectionTitle("Scheme")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeDot(Color.White, currentScheme == ColorScheme.LIGHT) { coroutineScope.launch { appSettingsStore.setColorScheme(ColorScheme.LIGHT) } }
            ThemeDot(Color.Black, currentScheme == ColorScheme.DARK) { coroutineScope.launch { appSettingsStore.setColorScheme(ColorScheme.DARK) } }
            ThemeDot(Color.Gray, currentScheme == ColorScheme.SYSTEM) { coroutineScope.launch { appSettingsStore.setColorScheme(ColorScheme.SYSTEM) } }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeColorsSetting() {
    val appConfigStore = GlobalStores.getAppConfigStore(LocalContext.current)
    val currentTheme by appConfigStore.getTheme().collectAsState(initial = ThemeName.Crimson)
    val coroutineScope = rememberCoroutineScope()
    LocalActivity.current

    val themeColors = remember {
        mapOf(
            ThemeName.Tomato to Color(0xffe54d2e),
            ThemeName.Red to Color(0xffe5484d),
            ThemeName.Crimson to Color(0xffE93D82),
            ThemeName.Pink to Color(0xffd6409f),
            ThemeName.Purple to Color(0xff8e4ec6),
            ThemeName.Indigo to Color(0xff3e63dd),
            ThemeName.Blue to Color(0xff0091ff),
            ThemeName.Cyan to Color(0xff05a2c2),
            ThemeName.Teal to Color(0xff12a594),
            ThemeName.Green to Color(0xff30a46c),
            ThemeName.Grass to Color(0xff46a758),
            ThemeName.Orange to Color(0xfff76808),
            ThemeName.Brown to Color(0xffad7f58),
            ThemeName.Sky to Color(0xff68ddfd),
            ThemeName.Mint to Color(0xff70e1c8),
            ThemeName.Yellow to Color(0xfff5d90a),
            ThemeName.Amber to Color(0xffffb224),
            ThemeName.Gray to Color(0xff8f8f8f),
            ThemeName.Sand to Color(0xff90908c),
            ThemeName.Gold to Color(0xff978365),
            ThemeName.Slate to Color(0xff889096)
        )
    }

    Column {
        SectionTitle("Thema")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeName.entries.forEach { themeName ->
                val color = themeColors[themeName] ?: Color.Black
                ThemeDot(color, themeName == currentTheme) {
                    coroutineScope.launch { appConfigStore.setTheme(themeName) }
                }
            }
        }
    }
}

@Composable
private fun ThemeDot(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
            .then(
                if (isSelected)
                    Modifier.border(2.dp, Color.White, CircleShape)
                else
                    Modifier
            )
    )
}