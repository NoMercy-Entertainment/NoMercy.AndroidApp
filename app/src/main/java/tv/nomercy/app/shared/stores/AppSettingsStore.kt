package tv.nomercy.app.shared.stores

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ColorScheme {
    LIGHT, DARK, SYSTEM
}

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppSettingsStore(context: Context) {

    private val dataStore = context.appSettingsDataStore

    private object PreferencesKeys {
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val LANGUAGE = stringPreferencesKey("language")
        val SCREENSAVER_DELAY = intPreferencesKey("screensaver_delay")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
    }

    val deviceName: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEVICE_NAME] ?: Build.MODEL
    }

    suspend fun setDeviceName(name: String) {
        dataStore.edit { it[PreferencesKeys.DEVICE_NAME] = name }
    }

    val language: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LANGUAGE] ?: "Nederlands"
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { it[PreferencesKeys.LANGUAGE] = language }
    }

    val screensaverDelay: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SCREENSAVER_DELAY] ?: 5
    }

    suspend fun setScreensaverDelay(delay: Int) {
        dataStore.edit { it[PreferencesKeys.SCREENSAVER_DELAY] = delay }
    }

    val colorScheme: Flow<ColorScheme> = dataStore.data.map { preferences ->
        ColorScheme.valueOf(preferences[PreferencesKeys.COLOR_SCHEME] ?: ColorScheme.DARK.name)
    }

    suspend fun setColorScheme(scheme: ColorScheme) {
        dataStore.edit { it[PreferencesKeys.COLOR_SCHEME] = scheme.name }
    }
}