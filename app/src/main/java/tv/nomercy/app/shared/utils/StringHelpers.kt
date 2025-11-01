package tv.nomercy.app.shared.utils

import android.content.Context
import androidx.annotation.PluralsRes
import tv.nomercy.app.R

fun pluralString(context: Context, @PluralsRes id: Int, quantity: Int): String {
    return context.resources.getQuantityString(id, quantity)
}

fun resolveGenre(context: Context, genre: String): String {
    return when (genre.lowercase()) {
        "adventure" -> context.getString(R.string.genre_adventure)
        "fantasy" -> context.getString(R.string.genre_fantasy)
        "animation" -> context.getString(R.string.genre_animation)
        "drama" -> context.getString(R.string.genre_drama)
        "horror" -> context.getString(R.string.genre_horror)
        "action" -> context.getString(R.string.genre_action)
        "comedy" -> context.getString(R.string.genre_comedy)
        "history" -> context.getString(R.string.genre_history)
        "western" -> context.getString(R.string.genre_western)
        "thriller" -> context.getString(R.string.genre_thriller)
        "crime" -> context.getString(R.string.genre_crime)
        "documentary" -> context.getString(R.string.genre_documentary)
        "science fiction" -> context.getString(R.string.genre_science_fiction)
        "mystery" -> context.getString(R.string.genre_mystery)
        "music" -> context.getString(R.string.genre_music)
        "romance" -> context.getString(R.string.genre_romance)
        "family" -> context.getString(R.string.genre_family)
        "war" -> context.getString(R.string.genre_war)
        "action & adventure" -> context.getString(R.string.genre_action_adventure)
        "kids" -> context.getString(R.string.genre_kids)
        "news" -> context.getString(R.string.genre_news)
        "reality" -> context.getString(R.string.genre_reality)
        "sci-fi & fantasy" -> context.getString(R.string.genre_sci_fi_fantasy)
        "soap" -> context.getString(R.string.genre_soap)
        "talk" -> context.getString(R.string.genre_talk)
        "war & politics" -> context.getString(R.string.genre_war_politics)
        "tv movie" -> context.getString(R.string.genre_tv_movie)
        else -> genre
    }
}