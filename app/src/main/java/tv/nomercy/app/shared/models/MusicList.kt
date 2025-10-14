package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MusicList (
    val id: String,
    val name: String,
    val cover: String? = null,
    val disambiguation: String? = null,
    val link: String,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val country: String? = null,
    val description: String? = null,
    val favorite: Boolean? = null,
    @SerialName("library_id")
    val libraryID: String,
    val year: Int? = null,
    val artists: List<Artist>? = emptyList(),
    val albums: List<Album>? = emptyList(),
    val tracks: List<PlaylistItem>,
    val type: String
)

@Serializable
data class Artist (
    val id: String,
    val name: String,
    val disambiguation: String? = null,
    val description: String? = null,
    val backdrop: String? = null,
    val cover: String? = null,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val link: String,
    val type: String,
)

@Serializable
data class Album (
    val id: String,
    val name: String,
    val backdrop: String? = null,
    val cover: String? = null,
    val disambiguation: String? = null,
    val link: String,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val description: String? = null,
    val year: Long? = null,
    @SerialName("album_artist")
    val albumArtist: String? = null,
    val type: String,
)

//@Serializable
//data class Track (
//    val id: String,
//    val name: String,
//    val cover: String? = null,
//    val path: String,
//    val link: String,
//    @SerialName("color_palette")
//    val colorPalette: ColorPalettes? = null,
//    val date: String? = null,
//    val disc: Int,
//    val duration: String,
//    val favorite: Boolean? = null,
//    val quality: Int,
//    val track: Int,
//    val lyrics: List<Lyric>? = null,
//    val type: String,
//    @SerialName("artist_track")
//    val artistTrack: List<Artist>,
//    @SerialName("album_track")
//    val albumTrack: List<Album>
//)

@Serializable
data class Lyric (
    val text: String,
    val time: Time
)

@Serializable
data class Time (
    val total: Double,
    val minutes: Long,
    val seconds: Long,
    val hundredths: Long
)
