package tv.nomercy.app.shared.models

import ComponentData
import kotlinx.serialization.SerialName

/**
 * Response wrapper for API responses
 */
data class ApiResponse<T>(
    val data: T?,
    val message: String?,
    val status: String?
)

/**
 * App configuration response containing user info, servers, and permissions
 */
data class AppConfig(
    val servers: List<Server>,
    val messages: List<Message>?,
    val notifications: List<Notification>?,
    val locale: String?,
    val name: String?,
    val avatarUrl: String?,
    val features: Map<String, Boolean>?,
    val moderator: Boolean?,
    val admin: Boolean?
)

/**
 * Server information
 */
data class Server(
    val id: String,
    val name: String,
    @SerialName("server_api_url")
    val serverApiUrl: String,
    val description: String?,
    val version: String?,
    val status: String?,
    @SerialName("is_owner")
    val isOwner: Boolean? = null,
    @SerialName("is_manager")
    val isManager: Boolean? = null
)

/**
 * Message model
 */
data class Message(
    val id: String,
    val title: String,
    val content: String,
    val type: String,
    val timestamp: String?
)

/**
 * Notification model
 */
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val timestamp: String?
)

/**
 * User profile information
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    val locale: String?,
    val features: Map<String, Boolean>?,
    val moderator: Boolean?,
    val admin: Boolean?
)

/**
 * Server permissions response
 */
data class PermissionsResponse(
    val owner: Boolean,
    val manager: Boolean
)

/**
 * Library model for server libraries
 */
data class Library(
    val id: String,
    val title: String,
    val type: String,
    val link: String,
    val autoRefreshInterval: Int?,
    val perfectSubtitleMatch: Boolean?,
    val realtime: Boolean?,
    val specialSeasonName: String?,
    val order: Int?,
    val blurHash: String?,
    val colorPalette: String?,
    val image: String?,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * Library response containing dynamic components
 * This matches your actual API response structure
 */
data class LibraryResponse<T: ComponentData>(
    val id: String,
    val data: Component<T>
)

