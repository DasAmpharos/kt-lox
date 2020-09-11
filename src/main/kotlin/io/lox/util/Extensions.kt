package io.lox.util

inline fun <T, K, V> Iterable<T>.toMap(fn: (T) -> Pair<K, V>): Map<K, V> {
    return map(fn).toMap()
}