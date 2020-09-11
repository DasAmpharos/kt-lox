package io.lox

import io.lox.util.Int32

data class Token(
    val type: Type,
    val lexeme: String,
    val literal: Any?,
    val line: Int32
) {
    enum class Type {
        // Single-character tokens.
        PAREN_L, PAREN_R, BRACE_L, BRACE_R,
        COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

        // One or two character tokens.
        BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

        // Literals.
        IDENTIFIER, STRING, NUMBER,

        // Keywords.
        AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
        PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

        EOF
    }

    companion object {
        val EOF = Token(Type.EOF, "", null, 0)
    }
}