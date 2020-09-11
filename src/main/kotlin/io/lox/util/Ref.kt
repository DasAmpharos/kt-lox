package io.lox.util

data class Ref<T>(
    var value: T
) {
    companion object {
        fun Ref<Int32>.inc() {
            value++
        }
    }
}
