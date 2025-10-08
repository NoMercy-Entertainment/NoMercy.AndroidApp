package tv.nomercy.app.shared.stores

import android.content.Context
import tv.nomercy.app.shared.api.DomainApiClient
import tv.nomercy.app.shared.api.services.AuthService

object GlobalStores {

    @Volatile private var authStoreInstance: AuthStore? = null
    @Volatile private var authServiceInstance: AuthService? = null
    @Volatile private var domainApiClientInstance: DomainApiClient? = null
    @Volatile private var serverConfigStoreInstance: ServerConfigStore? = null
    @Volatile private var librariesStoreInstance: LibrariesStore? = null
    @Volatile private var libraryStoreInstance: LibraryStore? = null
    @Volatile private var homeStoreInstance: HomeStore? = null
    @Volatile private var infoStoreInstance: InfoStore? = null
    @Volatile private var appConfigStoreInstance: AppConfigStore? = null
    @Volatile private var themeDataStoreInstance: ThemeDataStore? = null
    @Volatile private var appSettingsStoreInstance: AppSettingsStore? = null

    fun getAppSettingsStore(context: Context): AppSettingsStore {
        return appSettingsStoreInstance ?: synchronized(this) {
            appSettingsStoreInstance ?: AppSettingsStore(context.applicationContext).also {
                appSettingsStoreInstance = it
            }
        }
    }

    fun getThemeDataStore(context: Context): ThemeDataStore {
        return themeDataStoreInstance ?: synchronized(this) {
            themeDataStoreInstance ?: ThemeDataStore(context.applicationContext).also {
                themeDataStoreInstance = it
            }
        }
    }

    fun getAuthStore(context: Context): AuthStore {
        return authStoreInstance ?: synchronized(this) {
            authStoreInstance ?: AuthStore(context.applicationContext).also {
                authStoreInstance = it
            }
        }
    }

    fun getAuthService(context: Context): AuthService {
        val authStore = getAuthStore(context)
        return authServiceInstance ?: synchronized(this) {
            authServiceInstance ?: AuthService(context.applicationContext, authStore).also {
                authServiceInstance = it
            }
        }
    }

    fun getDomainApiClient(context: Context): DomainApiClient {
        val authStore = getAuthStore(context)
        val authService = getAuthService(context)
        return domainApiClientInstance ?: synchronized(this) {
            domainApiClientInstance ?: DomainApiClient(context.applicationContext, authService, authStore).also {
                domainApiClientInstance = it
            }
        }
    }

    fun getAppConfigStore(context: Context): AppConfigStore {
        val authStore = getAuthStore(context)
        val themeDataStore = getThemeDataStore(context)

        return appConfigStoreInstance ?: synchronized(this) {
            appConfigStoreInstance ?: AppConfigStore(
                context.applicationContext,
                authStore,
                themeDataStore
            ).also {
                appConfigStoreInstance = it
            }
        }
    }

    fun getServerConfigStore(context: Context): ServerConfigStore {
        val authStore = getAuthStore(context)
        val authService = getAuthService(context)
        val appConfigStore = getAppConfigStore(context) // safe now

        return serverConfigStoreInstance ?: synchronized(this) {
            serverConfigStoreInstance ?: ServerConfigStore(
                context.applicationContext,
                authService,
                authStore,
                appConfigStore
            ).also {
                serverConfigStoreInstance = it
            }
        }
    }



    fun getHomeStore(context: Context): HomeStore {
        val authStore = getAuthStore(context)
        val serverConfigStore = getServerConfigStore(context)

        return homeStoreInstance ?: synchronized(this) {
            homeStoreInstance ?: HomeStore(
                context.applicationContext,
                authStore,
                serverConfigStore
            ).also {
                homeStoreInstance = it
            }
        }
    }

    fun getInfoStore(context: Context): InfoStore {
        val authStore = getAuthStore(context)
        val serverConfigStore = getServerConfigStore(context)

        return infoStoreInstance ?: synchronized(this) {
            infoStoreInstance ?: InfoStore(
                context.applicationContext,
                authStore,
                serverConfigStore
            ).also {
                infoStoreInstance = it
            }
        }
    }

    fun getLibraryStore(context: Context): LibraryStore {
        val authStore = getAuthStore(context)
        val serverConfigStore = getServerConfigStore(context)
        return libraryStoreInstance ?: synchronized(this) {
            libraryStoreInstance ?: LibraryStore(
                context.applicationContext,
                authStore,
                serverConfigStore
            ).also {
                libraryStoreInstance = it
            }
        }
    }

    fun getLibrariesStore(context: Context): LibrariesStore {
        val authStore = getAuthStore(context)
        val serverConfigStore = getServerConfigStore(context)
        return librariesStoreInstance ?: synchronized(this) {
            librariesStoreInstance ?: LibrariesStore(
                context.applicationContext,
                authStore,
                serverConfigStore
            ).also {
                librariesStoreInstance = it
            }
        }
    }


    fun clearAll() {
        synchronized(this) {
            authStoreInstance?.clearData()
            appConfigStoreInstance?.clearData()
            serverConfigStoreInstance?.clearData()
            libraryStoreInstance?.clearData()
            homeStoreInstance?.clearData()

            authStoreInstance = null
            authServiceInstance = null
            domainApiClientInstance = null
            appConfigStoreInstance = null
            serverConfigStoreInstance = null
            libraryStoreInstance = null
            homeStoreInstance = null
            appSettingsStoreInstance = null
        }
    }
}
