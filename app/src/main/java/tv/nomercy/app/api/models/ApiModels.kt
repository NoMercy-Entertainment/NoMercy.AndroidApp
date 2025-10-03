package tv.nomercy.app.api.models

import com.google.gson.annotations.SerializedName

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
    @SerializedName("server_api_url")
    val serverApiUrl: String,
    val description: String?,
    val version: String?,
    val status: String?,
    @SerializedName("is_owner")
    val isOwner: Boolean? = null,
    @SerializedName("is_manager")
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
    @SerializedName("avatar_url")
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
data class LibraryResponse(
    val id: String,
    val data: Component<MediaItem>
)

/**
 * Media item model for library contents
 */
data class MediaItem (
    val id: String,
    val backdrop: String,
    val favorite: Boolean,
    val watched: Boolean,
    val logo: String,
    @SerializedName("media_type")
    val mediaType: String?,
    @SerializedName("number_of_items")
    val numberOfItems: Long,
    @SerializedName("have_items")
    val haveItems: Long,
    val overview: String,
    @SerializedName("color_palette")
    val colorPalette: ColorPalettes?,
    val poster: String,
    val title: String,
    val name: String? = null,
    val titleSort: String,
    val type: String,
    val year: Long,
    @SerializedName("videoId")
    val videoID: String?,
    val link: String,
    val genres: List<Genre>,
    val videos: List<Video>
)

data class ColorPalettes (
    val poster: PaletteColors? = null,
    val backdrop: PaletteColors? = null,
    val logo: PaletteColors? = null,
    val image: PaletteColors? = null,
    val cover: PaletteColors? = null
)

data class PaletteColors (
    val dominant: String,
    val primary: String,
    val lightVibrant: String,
    val darkVibrant: String,
    val lightMuted: String,
    val darkMuted: String
)

data class Genre (
    val id: Long,
    val name: String,
    val link: String
)

data class Video (
    val src: String,
    val type: String,
    val name: String,
    val site: String,
    val size: Long
)
