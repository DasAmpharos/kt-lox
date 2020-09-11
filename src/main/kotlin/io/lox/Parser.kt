package io.lox

import java.util.*

object Parser {
    fun parse(tokens: List<Token>): List<Stmt> {
        val cursor = Cursor(tokens)
        val statements = LinkedList<Stmt>()
        while (!cursor.isEof()) {
            val stmt = declaration(cursor)
            if (stmt != null) statements += stmt
        }
        return statements
    }

    private fun declaration(cursor: Cursor): Stmt? {
        return when {
            cursor.matchNext(Token.Type.VAR) -> varDeclaration(cursor)
            else -> statement(cursor)
        }
    }

    private fun varDeclaration(cursor: Cursor): Stmt {

    }

    private fun statement(cursor: Cursor): Stmt {

    }

    private class Cursor(
        private val tokens: List<Token>
    ) {
        private var pos = 0

        fun peek(): Token {
            if (isEof()) return Token.EOF
            return tokens[pos]
        }

        fun next(): Token {
            if (isEof()) return Token.EOF
            return tokens[pos++]
        }

        fun previous(): Token {
            return tokens[pos - 1]
        }

        fun matchNext(vararg types: Token.Type): Boolean {
            val next = peek()
            val matched = types.any { next.type == it }
            if (matched) pos++
            return matched
        }

        fun isEof(): Boolean {
            return pos >= tokens.size || tokens[pos].type == Token.Type.EOF
        }
    }
}