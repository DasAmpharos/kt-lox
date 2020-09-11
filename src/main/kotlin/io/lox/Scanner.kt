package io.lox

import io.lox.util.Int32
import io.lox.util.Ref
import io.lox.util.Ref.Companion.inc
import java.util.*

object Scanner {
    fun scanTokens(source: String): List<Token> {
        val line = Ref(1)
        val cursor = Cursor(source)
        val tokens = LinkedList<Token>()
        while (!cursor.isEof()) {
            skipSpaces(cursor, line)
            skipComments(cursor, line)
            scanToken(source, tokens, cursor, line)
        }
        tokens += Token.EOF
        return tokens
    }

    private fun skipSpaces(
        cursor: Cursor,
        line: Ref<Int32>
    ) {
        while (!cursor.isEof()) {
            val char = cursor.peek()
            if (!char.isWhitespace()) break
            if (char == '\n') line.inc()
            cursor.next()
        }
    }

    private fun skipComments(
        cursor: Cursor,
        line: Ref<Int>
    ) {
        if (cursor.peek() == '/' && cursor.peek(1) == '/') {
            cursor.next(); cursor.next()
            while (!cursor.isEof()) {
                if (cursor.matchNext('\n')) {
                    line.inc()
                    break
                }
                cursor.next()
            }
        }
    }

    private fun scanToken(
        source: String,
        tokens: MutableList<Token>,
        cursor: Cursor,
        line: Ref<Int>
    ) {
        if (cursor.isEof()) return

        val start = cursor.pos
        val type = when (val char = cursor.next()) {
            '(' -> Token.Type.PAREN_L
            ')' -> Token.Type.PAREN_R
            '{' -> Token.Type.BRACE_L
            '}' -> Token.Type.BRACE_R
            '.' -> Token.Type.DOT
            ',' -> Token.Type.COMMA
            ';' -> Token.Type.SEMICOLON
            '+' -> Token.Type.PLUS
            '-' -> Token.Type.MINUS
            '*' -> Token.Type.STAR
            '/' -> Token.Type.SLASH
            '!' -> if (cursor.matchNext('='))
                Token.Type.BANG_EQUAL else Token.Type.BANG
            '=' -> if (cursor.matchNext('='))
                Token.Type.EQUAL_EQUAL else Token.Type.EQUAL
            '<' -> if (cursor.matchNext('='))
                Token.Type.LESS_EQUAL else Token.Type.LESS
            '>' -> if (cursor.matchNext('='))
                Token.Type.GREATER_EQUAL else Token.Type.GREATER
            '"' -> Token.Type.STRING
            else -> when {
                isDigit(char) -> Token.Type.NUMBER
                isAlpha(char) -> Token.Type.IDENTIFIER
                else -> {
                    Lox.error(0, "Unexpected character.")
                    null
                }
            }
        }
        if (type != null) {
            when (type) {
                Token.Type.STRING -> scanString(source, tokens, cursor, line, start)
                Token.Type.NUMBER -> scanNumber(source, tokens, cursor, line, start)
                Token.Type.IDENTIFIER -> scanIdentifier(source, tokens, cursor, line, start)
                else -> scanSimpleToken(source, tokens, cursor, type, line, start)
            }
        }
    }

    private fun scanSimpleToken(
        source: String,
        tokens: MutableList<Token>,
        cursor: Cursor,
        type: Token.Type,
        line: Ref<Int>,
        start: Int
    ) {
        val lexeme = source.substring(start, cursor.pos)
        tokens += Token(type, lexeme, null, line.value)
    }
    private fun scanString(
        source: String,
        tokens: MutableList<Token>,
        cursor: Cursor,
        line: Ref<Int>,
        start: Int
    ) {
        while (cursor.peek() != '"' && !cursor.isEof()) {
            if (cursor.peek() == '\n') line.inc()
            cursor.next()
        }

        // Unterminated string.
        if (cursor.isEof()) {
            Lox.error(line.value, "Unterminated string.")
            return
        }

        // the closing ".
        cursor.next()

        // Trim the surrounding quotes.
        val lexeme = source.substring(start, cursor.pos)
        val value = lexeme.substring(1, lexeme.length - 1)
        tokens += Token(Token.Type.STRING, lexeme, value, line.value)
    }

    private fun scanNumber(
        source: String,
        tokens: MutableList<Token>,
        cursor: Cursor,
        line: Ref<Int>,
        start: Int
    ) {
        while (isDigit(cursor.peek())) cursor.next()

        // Look for a fractional part
        if (cursor.peek() == '.' && isDigit(cursor.peek(1))) {
            cursor.next() // Consume the "."
            while (isDigit(cursor.peek())) cursor.next()
        }

        val lexeme = source.substring(start, cursor.pos)
        tokens += Token(Token.Type.NUMBER, lexeme, lexeme.toDouble(), line.value)
    }

    private fun scanIdentifier(
        source: String,
        tokens: MutableList<Token>,
        cursor: Cursor,
        line: Ref<Int>,
        start: Int
    ) {
        while (isAlphaNumeric(cursor.peek())) cursor.next()
        val lexeme = source.substring(start, cursor.pos)
        val type = Keywords[lexeme] ?: Token.Type.IDENTIFIER
        tokens += Token(type, lexeme, null, line.value)
    }

    private fun isAlpha(char: Char): Boolean {
        return char in 'a'..'z' || char in 'A'..'Z' || char == '_'
    }

    private fun isDigit(char: Char): Boolean {
        return char in '0'..'9'
    }

    private fun isAlphaNumeric(char: Char): Boolean {
        return isAlpha(char) || isDigit(char)
    }

    private class Cursor(
        private val source: String
    ) {
        var pos = 0
            private set

        fun next(): Char {
            return when {
                isEof() -> Char.MIN_VALUE
                else -> source[pos++]
            }
        }

        fun peek(lookahead: Int32 = 0): Char {
            return when {
                isEof(lookahead) -> Char.MIN_VALUE
                else -> source[pos + lookahead]
            }
        }

        fun matchNext(expected: Char): Boolean {
            val matches = !isEof() && peek() == expected
            if (matches) pos++
            return matches
        }

        fun isEof(lookahead: Int32 = 0): Boolean {
            return pos + lookahead >= source.length
        }

    }
}