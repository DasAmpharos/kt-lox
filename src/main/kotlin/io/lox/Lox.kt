package io.lox

import io.lox.util.Int32

object Lox {
    var hadError = false
        private set
    var hadRuntimeError = false
        private set

    fun error(line: Int32, message: String) {
        report(line, null, message)
    }

    fun error(token: Token, message: String) {
        if (token.type == Token.Type.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }

    private fun report(line: Int, where: String? = null, message: String) {
        var s = "[line $line] Error"
        if (where != null) s += where
        s += ": $message"
        System.err.println(s)
    }
}