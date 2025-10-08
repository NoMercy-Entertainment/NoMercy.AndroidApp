package tv.nomercy.app.shared.utils

import android.content.Context
import androidx.annotation.PluralsRes

fun pluralString(context: Context, @PluralsRes id: Int, quantity: Int): String {
    return context.resources.getQuantityString(id, quantity)
}