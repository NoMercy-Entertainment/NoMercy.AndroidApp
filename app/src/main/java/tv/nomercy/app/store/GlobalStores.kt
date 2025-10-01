package tv.nomercy.app.store

import android.content.Context

/**
 * Global store manager to ensure singleton instances
 * This prevents multiple store instances and ensures true global state
 */
object GlobalStores {

    @Volatile
    private var authStoreInstance: AuthStore? = null

    @Volatile
    private var appConfigStoreInstance: AppConfigStore? = null

    @Volatile
    private var authServiceInstance: tv.nomercy.app.auth.AuthService? = null

    @Volatile
    private var domainApiClientInstance: tv.nomercy.app.api.DomainApiClient? = null

    @Volatile
    private var libraryStoreInstance: LibraryStore? = null

    /**
     * Get the singleton AuthStore instance
     */
    fun getAuthStore(context: Context): AuthStore {
        return authStoreInstance ?: synchronized(this) {
            authStoreInstance ?: AuthStore(context.applicationContext).also {
                authStoreInstance = it
            }
        }
    }

    /**
     * Get the singleton AuthService instance
     */
    fun getAuthService(context: Context): tv.nomercy.app.auth.AuthService {
        val authStore = getAuthStore(context)
        return authServiceInstance ?: synchronized(this) {
            authServiceInstance ?: tv.nomercy.app.auth.AuthService(context.applicationContext, authStore).also {
                authServiceInstance = it
            }
        }
    }

    /**
     * Get the singleton AppConfigStore instance
     */
    fun getAppConfigStore(context: Context): AppConfigStore {
        val authStore = getAuthStore(context)
        val authService = getAuthService(context)

        return appConfigStoreInstance ?: synchronized(this) {
            appConfigStoreInstance ?: AppConfigStore(context.applicationContext, authService, authStore).also {
                appConfigStoreInstance = it
            }
        }
    }

    /**
     * Get the singleton DomainApiClient instance with proper authentication
     */
    fun getDomainApiClient(context: Context): tv.nomercy.app.api.DomainApiClient {
        val authStore = getAuthStore(context)
        val authService = getAuthService(context)

        return domainApiClientInstance ?: synchronized(this) {
            domainApiClientInstance ?: tv.nomercy.app.api.DomainApiClient(context.applicationContext, authService, authStore).also {
                domainApiClientInstance = it
            }
        }
    }

    /**
     * Get the singleton LibraryStore instance
     */
    fun getLibraryStore(context: Context): LibraryStore {
        val authStore = getAuthStore(context)
        val appConfigStore = getAppConfigStore(context)

        return libraryStoreInstance ?: synchronized(this) {
            libraryStoreInstance ?: LibraryStore(context.applicationContext, authStore, appConfigStore).also {
                libraryStoreInstance = it
            }
        }
    }

    /**
     * Clear all store instances (for logout)
     */
    fun clearAll() {
        synchronized(this) {
            authStoreInstance?.clearAuth()
            appConfigStoreInstance?.clearData()
            libraryStoreInstance?.clearLibraryData()
            authStoreInstance = null
            appConfigStoreInstance = null
            authServiceInstance = null
            domainApiClientInstance = null
            libraryStoreInstance = null
        }
    }
}
