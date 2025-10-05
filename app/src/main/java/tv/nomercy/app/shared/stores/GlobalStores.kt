package tv.nomercy.app.shared.stores

import android.content.Context
import tv.nomercy.app.shared.api.DomainApiClient
import tv.nomercy.app.shared.api.services.AuthService

object GlobalStores {

    @Volatile private var authStoreInstance: AuthStore? = null
    @Volatile private var authServiceInstance: AuthService? = null
    @Volatile private var domainApiClientInstance: DomainApiClient? = null
    @Volatile private var serverConfigStoreInstance: ServerConfigStore? = null
    @Volatile private var libraryStoreInstance: LibraryStore? = null
    @Volatile private var appConfigStoreInstance: AppConfigStore? = null

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

    fun getAppConfigStore(context: Context): AppConfigStore {
        val authStore = getAuthStore(context)

        return appConfigStoreInstance ?: synchronized(this) {
            appConfigStoreInstance ?: AppConfigStore(
                context.applicationContext,
                authStore
            ).also {
                appConfigStoreInstance = it
            }
        }
    }

    fun clearAll() {
        synchronized(this) {
            authStoreInstance?.clearData()
            appConfigStoreInstance?.clearData()
            serverConfigStoreInstance?.clearData()
            libraryStoreInstance?.clearData()

            authStoreInstance = null
            authServiceInstance = null
            domainApiClientInstance = null
            appConfigStoreInstance = null
            serverConfigStoreInstance = null
            libraryStoreInstance = null
        }
    }
}
