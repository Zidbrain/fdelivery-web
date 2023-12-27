package io.github.zidbrain.fdelivery.util

inline fun <T> MutableList<T>.addOrReplace(predicate: (T) -> Boolean, replaceWith: (T?) -> T): MutableList<T> {
    val index = indexOfFirst(predicate)
    if (index == -1) add(replaceWith(null))
    else add(replaceWith(get(index)))
    return this
}

inline fun <T> MutableList<T>.replace(predicate: (T) -> Boolean, with: (T) -> T) {
    val index = indexOfFirst(predicate)
    set(index, with(get(index)))
}