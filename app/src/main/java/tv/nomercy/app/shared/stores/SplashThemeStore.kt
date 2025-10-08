package tv.nomercy.app.shared.stores

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tv.nomercy.app.R
import tv.nomercy.app.shared.layout.SplashTheme

val Context.splashThemeDataStore: DataStore<Preferences> by preferencesDataStore(name = "splash_theme")

class SplashThemeStore(private val context: Context) {
    private val dataStore = context.splashThemeDataStore

    companion object {
        val StartColorKey = intPreferencesKey("startColor")
        val EndColorKey = intPreferencesKey("endColor")
        val IconTintKey = intPreferencesKey("iconTint")
    }

    val themeFlow: Flow<SplashTheme> = dataStore.data.map { prefs ->
        SplashTheme(
            backgroundStartColor = Color(prefs[StartColorKey] ?: R.color.crimson_12.toInt()),
            backgroundEndColor = Color(prefs[EndColorKey] ?: R.color.crimson_11.toInt()),
            iconTint = Color(prefs[IconTintKey] ?: R.color.crimson_1.toInt())
        )
    }

    suspend fun saveTheme(theme: SplashTheme) {
        dataStore.edit { prefs ->
            prefs[StartColorKey] = theme.backgroundStartColor.value.toInt()
            prefs[EndColorKey] = theme.backgroundEndColor.value.toInt()
            prefs[IconTintKey] = theme.iconTint.value.toInt()
        }
    }

    suspend fun clearTheme() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}