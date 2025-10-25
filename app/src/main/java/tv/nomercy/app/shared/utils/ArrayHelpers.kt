package tv.nomercy.app.shared.utils

inline fun <T, K, V> List<T>.sortByFilteredAlphabetized(
    crossinline keySelector: (T) -> K,             // e.g. name
    crossinline valueSelector: (T) -> V,           // e.g. department
    crossinline sortSelector: (T) -> String?,      // e.g. name or title
    crossinline filterSelector: (T) -> Boolean     // e.g. profile != null
): List<T> {
    val grouped = mutableMapOf<K, MutableList<V>>()
    val finalList = mutableListOf<T>()

    forEach { item ->
        val key = keySelector(item)
        val value = valueSelector(item)

        val group = grouped.getOrPut(key) {
            finalList.add(item)
            mutableListOf()
        }
        group.add(value)
    }

    val comparator = compareBy<T> { sortSelector(it)?.lowercase() }

    val filtered = finalList.filter(filterSelector).sortedWith(comparator)
    val unfiltered = finalList.filterNot(filterSelector).sortedWith(comparator)

    return filtered + unfiltered
}