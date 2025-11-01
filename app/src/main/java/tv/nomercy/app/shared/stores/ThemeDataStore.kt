package tv.nomercy.app.shared.stores

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tv.nomercy.app.shared.ui.ThemeName

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemeDataStore(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val THEME_NAME = stringPreferencesKey("theme_name")
    }

    val getTheme: Flow<ThemeName> = dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_NAME] ?: ThemeName.Crimson.name
            ThemeName.valueOf(themeName)
        }

    suspend fun setTheme(themeName: ThemeName) {
        dataStore.edit {
            it[PreferencesKeys.THEME_NAME] = themeName.name
        }
    }
}