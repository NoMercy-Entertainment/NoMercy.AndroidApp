package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Response wrapper for API responses
 */
@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val status: String? = null
)

/**
 * App configuration response containing user info, servers, and permissions
 */
@Serializable
data class AppConfig(
    val servers: List<Server>,
    val messages: List<Message>? = emptyList(),
    val notifications: List<Notification>? = emptyList(),
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
@Serializable
data class Server(
    val id: String,
    val name: String,
    @SerialName("server_api_url") val serverApiUrl: String,
    val description: String? = null,
    val version: String? = null,
    val status: String? = null,
    @SerialName("is_owner") val isOwner: Boolean? = null,
    @SerialName("is_manager") val isManager: Boolean? = null
)

/**
 * Message model
 */
@Serializable
data class Message(
    val id: Int,
    val title: String? = null,
    @SerialName("body") val content: String,
    val type: String,
    val read: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val from: Sender? = null
)

@Serializable
data class Sender(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null
)
/**
 * Notification model
 */
@Serializable
data class Notification(
    val id: Int,
    val title: String? = null,
    @SerialName("body") val message: String,
    val type: String,
    val read: Boolean,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val from: JsonElement? = null // can be string or object
)

/**
 * User profile information
 */
@Serializable
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
@Serializable
data class PermissionsResponse(
    val owner: Boolean,
    val manager: Boolean
)

/**
 * Library model for server libraries
 */
@Serializable
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
    val image: String?,
)
