package io.lox

import io.lox.Token.Type
import io.lox.util.toMap

object Keywords {
    private val keywords = listOf(
        Type.AND,
        Type.CLASS,
        Type.ELSE,
        Type.FALSE,
        Type.FUN,
        Type.FOR,
        Type.IF,
        Type.NIL,
        Type.OR,
        Type.PRINT,
        Type.RETURN,
        Type.SUPER,
        Type.THIS,
        Type.TRUE,
        Type.VAR,
        Type.WHILE
    ).toMap {
        it.name.toLowerCase() to it
    }

    operator fun get(key: String): Type? = keywords[key]
}