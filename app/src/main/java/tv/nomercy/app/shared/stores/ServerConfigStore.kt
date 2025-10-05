package tv.nomercy.app.shared.stores

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.models.Server

class ServerConfigStore(
    private val context: Context,
    private val authService: AuthService,
    private val authStore: AuthStore,
    private val appConfigStore: AppConfigStore
) {
    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers = _servers.asStateFlow()

    private val _currentServer = MutableStateFlow<Server?>(null)
    val currentServer = _currentServer.asStateFlow()

    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    init {
        observeUserServers()
    }

    private fun observeUserServers() {
        CoroutineScope(Dispatchers.IO).launch {
            appConfigStore.servers.collect { newServers ->
                if (newServers.isNotEmpty()) {
                    _servers.value = newServers
                    handleServerSelection()
                }
            }
        }
    }

    fun setServers(servers: List<Server>) {
        _servers.value = servers
        handleServerSelection()
    }

    fun setCurrentServer(server: Server) {
        _currentServer.value = server
        saveSelectedServerId(server.id)

        val libraryStore = GlobalStores.getLibraryStore(context)
        libraryStore.fetchLibraries()
    }

    fun getServerApiClient(): ServerApiClient? {
        return _currentServer.value?.let {
            ServerApiClient.create(it.serverApiUrl, context, authService, authStore)
        }
    }

    suspend fun fetchServerPermissions(): Result<Unit> {
        val server = _currentServer.value ?: return Result.success(Unit)
        val client = getServerApiClient() ?: return Result.failure(Exception("No API client"))

        return try {
            val service = client.createService<ServerApiService>()
            val response = service.getServerPermissions()
            val perms = response.body()?.data ?: return Result.failure(Exception("No permission data"))

            val updated = server.copy(isOwner = perms.owner, isManager = perms.manager)
            _currentServer.value = updated
            _servers.value = _servers.value.map { if (it.id == updated.id) updated else it }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearData() {
        _servers.value = emptyList()
        _currentServer.value = null
        clearSelectedServerId()
    }

    private fun handleServerSelection() {
        val servers = _servers.value
        val savedId = getSavedServerId()
        val match = servers.find { it.id == savedId }

        when {
            servers.isEmpty() -> return
            servers.size == 1 -> setCurrentServer(servers[0])
            match != null -> setCurrentServer(match)
        }
    }

    private fun saveSelectedServerId(id: String) {
        sharedPrefs.edit { putString("selected_server_id", id) }
    }

    private fun getSavedServerId(): String? {
        return sharedPrefs.getString("selected_server_id", null)
    }

    private fun clearSelectedServerId() {
        sharedPrefs.edit { remove("selected_server_id") }
    }
}