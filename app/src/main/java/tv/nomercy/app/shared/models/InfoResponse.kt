package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InfoResponse(
    val id: Int,
    val duration: Int? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val logo: String? = null,
    val title: String,
    val name: String? = null,
    val overview: String? = null,
    val titleSort: String,
    val voteAverage: Double? = null,
    @SerialName("content_ratings")
    val contentRatings: List<Rating> = emptyList(),
    val year: Int,
    @SerialName("number_of_items")
    val numberOfItems: Int? = null,
    @SerialName("have_items")
    val haveItems: Int? = null,
    val backdrops: List<Image> = emptyList(),
    val posters: List<Image> = emptyList(),
    val logos: List<Image> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val creators: List<Person> = emptyList(),
    val directors: List<Person> = emptyList(),
    val writers: List<Person> = emptyList(),
    val keywords: List<String> = emptyList(),
    val budget: Long? = null,
    val type: String,
    @SerialName("media_type")
    val mediaType: String,
    val favorite: Boolean,
    val watched: Boolean,
    @SerialName("external_ids")
    val externalIds: ExternalIds,
    val cast: List<Person> = emptyList(),
    val crew: List<Person> = emptyList(),
    val director: Person? = null,
    val writer: Person? = null,
    val creator: Person? = null,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val link: String,
    val videos: List<ExtendedVideo> = emptyList(),
    val similar: List<Related> = emptyList(),
    val recommendations: List<Related> = emptyList(),
    val seasons: List<Season> = emptyList(),
    @SerialName("watch_providers")
    val watchProviders: List<WatchProvider> = emptyList(),
    val companies: List<Company> = emptyList(),
    val networks: List<Network> = emptyList()
)

@Serializable
data class Season(
    val id: Int,
    val overview: String,
    val poster: String? = null,
    @SerialName("season_number")
    val seasonNumber: Int,
    val title: String,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes,
    val episodes: List<Episode> = emptyList()
)

@Serializable
data class Image (
    val height: Long,
    val id: Long,
    val src: String,
    val type: String,
    val width: Long,
    @SerialName("iso_639_1")
    val iso6391: String? = null,
    val voteAverage: Double,
    val voteCount: Long,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes,
)


@Serializable
data class Company(
    val id: Int,
    @SerialName("logo_path")
    val logoPath: String? = null,
    val name: String,
    @SerialName("origin_country")
    val originCountry: String
)

@Serializable
data class Network(
    val name: String,
    val id: Int,
    @SerialName("logo_path")
    val logoPath: String? = null,
    @SerialName("origin_country")
    val originCountry: String
)

@Serializable
data class WatchProvider(
    val id: String,
    @SerialName("provider_id")
    val providerId: Int,
    @SerialName("country_code")
    val countryCode: String,
    val type: String,
    val link: String,
    val name: String,
    val logo: String,
    @SerialName("display_priority")
    val displayPriority: Int
)

@Serializable
data class Episode(
    val id: Int,
    @SerialName("episode_number")
    val episodeNumber: Int? = null,
    @SerialName("season_number")
    val seasonNumber: Int? = null,
    val title: String,
    val overview: String,
    val airDate: String? = null,
    val still: String? = null,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes,
    val progress: Int? = null,
    val available: Boolean,
    val link: String
)


@Serializable
data class MediaItem(
    val blurHash: String? = null,
    val aspectRatio: Double? = null,
    val height: Int,
    val id: Int,
    @SerialName("iso_639_1")
    val iso6391: String? = null,
    val name: String? = null,
    val site: String? = null,
    val size: String? = null,
    val profilePath: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val src: String,
    val type: String,
    val voteAverage: Double,
    val voteCount: Int,
    val width: Int,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null
)

@Serializable
data class Person (
    val character: String? = null,
    val job: String? = null,
    val profile: String? = null,
    val gender: Gender? = null,
    val id: Long,
    @SerialName("known_for_department")
    val knownForDepartment: String? = null,
    val name: String,
    val popularity: Double? = null,
    val deathday: String? = null,
    val translations: List<Translation>,
    val order: String? = null,
    val link: String,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null
)

@Serializable
data class Translation (
    val name: String? = null,
    val biography: String? = null,

    @SerialName("english_name")
    val englishName: String? = null,

    @SerialName("iso_639_1")
    val iso6391: String? = null,

    @SerialName("iso_3166_1")
    val iso31661: String? = null,
)

@Serializable
enum class Gender {
    Unknown,
    Female,
    Male,
    NonBinary
}

@Serializable
data class ExternalIds(
    @SerialName("imdb_id")
    val imdbId: String? = null,
    @SerialName("tvdb_id")
    val tvdbId: Int? = null
)

@Serializable
data class Related(
    val backdrop: String? = null,
    val id: Long,
    val overview: String? = null,
    val poster: String,
    val title: String,
    val titleSort: String,
    @SerialName("media_type")
    val mediaType: String,
    @SerialName("number_of_items")
    val numberOfItems: Int? = null,
    @SerialName("have_items")
    val haveItems: Int? = null,
    val link: String,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null
)
