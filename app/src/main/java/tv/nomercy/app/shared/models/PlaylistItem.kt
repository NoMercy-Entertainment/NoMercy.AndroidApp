package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasePlaylistItem(
    val id: String,
    val name: String,
    val cover: String?,
    val path: String,
    val disc: Int,
    val track: Int,
    @SerialName("album_track")
    val albumTrack: List<Album>,
    @SerialName("artist_track")
    val artistTrack: List<Artist>,
)

@Serializable
data class PlaylistItem(
    val id: String,
    val name: String,
    val cover: String? = null,
    val path: String,
    val link: String,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val date: String? = null,
    val disc: Int,
    val duration: String,
    val favorite: Boolean? = null,
    val quality: Int,
    val track: Int,
    val lyrics: List<Lyric>? = null,
    val type: String,
    @SerialName("artist_track")
    val artistTrack: List<Artist>,
    @SerialName("album_track")
    val albumTrack: List<Album>
)