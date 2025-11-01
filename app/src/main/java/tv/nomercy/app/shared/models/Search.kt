package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultElement (
    val adult: Boolean,
    val id: Long,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,

    @SerialName("media_type")
    val mediaType: String,

    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("profile_path")
    val profilePath: String? = null,

    @SerialName("original_title")
    val originalTitle: String? = null,
    @SerialName("original_language")
    val originalLanguage: String,
    @SerialName("genre_ids")
    val genreIDS: List<Long> = emptyList(),
    val popularity: Double? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    val video: Boolean? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("vote_count")
    val voteCount: Long? = null,
    @SerialName("original_name")
    val originalName: String? = null,
    @SerialName("first_air_date")
    val firstAirDate: String? = null,
    @SerialName("origin_country")
    val originCountry: List<String>? = null
)