package tv.nomercy.app.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

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
@Serializable
data class LibraryResponse(
    val id: String,
    val data: Component<MediaItem>
)

/**
 * Media item model for library contents
 */
@Serializable
data class MediaItem(
    val id: String,
    val title: String? = null,
    val name: String? = null,
    val type: String,
    val year: Int? = null,
    val overview: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val logo: String? = null,
    val rating: Double? = null,
    val voteAverage: Double? = null,
    val voteCount: Int? = null,
    val adult: Boolean? = null,
    val releaseDate: String? = null,
    val runtime: Int? = null,
    val genres: List<Genre>? = null,
    val seasons: Int? = null, // For TV shows
    val episodes: Int? = null, // For TV shows
    val status: String? = null, // For TV shows
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @kotlinx.serialization.SerialName("media_type")
    val mediaType: String? = null,
    @kotlinx.serialization.SerialName("number_of_items")
    val numberOfItems: Int? = null,
    @kotlinx.serialization.SerialName("have_items")
    val haveItems: Int? = null,
    @kotlinx.serialization.SerialName("color_palette")
    val colorPalette: Map<String, Map<String, String>>? = null,
    val favorite: Boolean? = null,
    val watched: Boolean? = null,
    val link: String,
    val videos: List<VideoInfo>? = null,
    @kotlinx.serialization.SerialName("titleSort")
    val titleSort: String? = null,
    @kotlinx.serialization.SerialName("videoId")
    val videoId: String? = null
)

/**
 * Video information
 */
@Serializable
data class VideoInfo(
    val src: String,
    val type: String,
    val name: String,
    val site: String,
    val size: Int
)

/**
 * Genre model
 */
@Serializable
data class Genre(
    val id: Int,
    val name: String,
    val link: String? = null
)

/**
 * Library statistics
 */
data class LibraryStats(
    val totalItems: Int,
    val movies: Int?,
    val tvShows: Int?,
    val albums: Int?,
    val artists: Int?,
    val songs: Int?,
    val collections: Int?
)
