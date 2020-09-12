package io.lox

import java.util.*

object Parser {
    fun parse(tokens: List<Token>): List<Stmt> {
        val cursor = Cursor(tokens)
        val statements = LinkedList<Stmt>()
        while (!cursor.isEof()) {
            try {
                statements += declaration(cursor)
            } catch (_: ParseError) {
                // ignore
            }
        }
        return statements
    }

    private fun declaration(cursor: Cursor): Stmt {
        return when {
            cursor.matchNext(Token.Type.VAR) -> varDeclaration(cursor)
            else -> statement(cursor)
        }
    }

    private fun varDeclaration(cursor: Cursor): Stmt {
        var initializer: Expr? = null
        val name = cursor.expectNext(Token.Type.IDENTIFIER, "Expected variable name.")
        if (cursor.matchNext(Token.Type.EQUAL)) {
            initializer = expression(cursor)
        }
        cursor.expectNext(Token.Type.SEMICOLON, "Expected ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(cursor: Cursor): Stmt {
        return when {
            cursor.matchNext(Token.Type.IF) -> ifStatement(cursor)
            cursor.matchNext(Token.Type.FOR) -> forStatement(cursor)
            cursor.matchNext(Token.Type.WHILE) -> whileStatement(cursor)
            cursor.matchNext(Token.Type.PRINT) -> printStatement(cursor)
            cursor.matchNext(Token.Type.BRACE_L) -> Stmt.Block(block(cursor))
            else -> expressionStatement(cursor)
        }
    }

    private fun ifStatement(cursor: Cursor): Stmt {
        cursor.expectNext(Token.Type.PAREN_L, "Expected '(' after 'if'.")
        val condition = expression(cursor)
        cursor.expectNext(Token.Type.PAREN_R, "Expected ')' after condition.")

        val thenBranch = statement(cursor)
        var elseBranch: Stmt? = null
        if (cursor.matchNext(Token.Type.ELSE)) {
            elseBranch = statement(cursor)
        }
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun forStatement(cursor: Cursor): Stmt {
        cursor.expectNext(Token.Type.PAREN_L, "Expected '(' after 'for'.")
        val initializer = when {
            cursor.matchNext(Token.Type.VAR) -> varDeclaration(cursor)
            cursor.matchNext(Token.Type.SEMICOLON) -> null
            else -> expressionStatement(cursor)
        }
        val condition = if (cursor.matchPeek(Token.Type.SEMICOLON))
            Expr.Literal(true) else expression(cursor)
        cursor.expectNext(Token.Type.SEMICOLON, "Expected ';' after loop condition.")
        val increment = if (cursor.matchPeek(Token.Type.PAREN_R)) null else expression(cursor)
        cursor.expectNext(Token.Type.PAREN_R, "Expected ')' after for clauses.")

        var body = statement(cursor)
        if (increment != null) {
            body = Stmt.Block(
                listOf(
                    body, Stmt.Expression(increment)
                )
            )
        }
        body = Stmt.While(condition, body)
        if (initializer != null) {
            body = Stmt.Block(
                listOf(
                    initializer, body
                )
            )
        }
        return body
    }

    private fun whileStatement(cursor: Cursor): Stmt.While {
        cursor.expectNext(Token.Type.PAREN_L, "Expected '(' after 'while'.")
        val condition = expression(cursor)
        cursor.expectNext(Token.Type.PAREN_R, "Expected ')' after condition.")
        val body = statement(cursor)
        return Stmt.While(condition, body)
    }

    private fun printStatement(cursor: Cursor): Stmt.Print {
        val value = expression(cursor)
        cursor.expectNext(Token.Type.SEMICOLON, "Expected ';' after value.")
        return Stmt.Print(value)
    }

    private fun block(cursor: Cursor): List<Stmt> {
        val statements = LinkedList<Stmt>()
        while (!cursor.isEof() && !cursor.matchPeek(Token.Type.BRACE_R)) {
            statements += declaration(cursor)
        }
        cursor.expectNext(Token.Type.BRACE_R, "Expected '}' after block.")
        return statements
    }

    private fun expressionStatement(cursor: Cursor): Stmt {
        val expr = expression(cursor)
        cursor.expectNext(Token.Type.SEMICOLON, "Expected ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun expression(cursor: Cursor): Expr {
        return assignment(cursor)
    }

    private fun assignment(cursor: Cursor): Expr {
        val expr = or(cursor)
        if (cursor.matchPeek(Token.Type.EQUAL)) {
            val equals = cursor.next()
            val value = assignment(cursor)
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun or(cursor: Cursor): Expr {
        var expr = and(cursor)
        while (cursor.matchPeek(Token.Type.OR)) {
            val op = cursor.next()
            val right = and(cursor)
            expr = Expr.Logical(op, expr, right)
        }
        return expr
    }

    private fun and(cursor: Cursor): Expr {
        var expr = equality(cursor)
        while (cursor.matchPeek(Token.Type.AND)) {
            val op = cursor.next()
            val right = equality(cursor)
            expr = Expr.Logical(op, expr, right)
        }
        return expr
    }

    private fun equality(cursor: Cursor): Expr {
        var expr = comparison(cursor)
        while (cursor.matchPeek(Token.Type.BANG_EQUAL, Token.Type.EQUAL_EQUAL)) {
            val op = cursor.next()
            val right = comparison(cursor)
            expr = Expr.Binary(op, expr, right)
        }
        return expr
    }

    private fun comparison(cursor: Cursor): Expr {
        var expr = addition(cursor)
        while (cursor.matchPeek(Token.Type.GREATER, Token.Type.GREATER_EQUAL, Token.Type.LESS, Token.Type.LESS_EQUAL)) {
            val op = cursor.next()
            val right = addition(cursor)
            expr = Expr.Binary(op, expr, right)
        }
        return expr
    }

    private fun addition(cursor: Cursor): Expr {
        var expr = multiplication(cursor)
        while (cursor.matchPeek(Token.Type.PLUS, Token.Type.MINUS)) {
            val op = cursor.next()
            val right = multiplication(cursor)
            expr = Expr.Binary(op, expr, right)
        }
        return expr
    }

    private fun multiplication(cursor: Cursor): Expr {
        var expr = unary(cursor)
        while (cursor.matchPeek(Token.Type.STAR, Token.Type.SLASH)) {
            val op = cursor.next()
            val right = unary(cursor)
            expr = Expr.Binary(op, expr, right)
        }
        return expr
    }

    private fun unary(cursor: Cursor): Expr {
        return if (cursor.matchPeek(Token.Type.MINUS, Token.Type.BANG)) {
            val op = cursor.next()
            val right = unary(cursor)
            Expr.Unary(op, right)
        } else {
            primary(cursor)
        }
    }

    private fun primary(cursor: Cursor): Expr {
        val next = cursor.next()
        return when (next.type) {
            Token.Type.NIL -> Expr.Literal(null)
            Token.Type.TRUE -> Expr.Literal(true)
            Token.Type.FALSE -> Expr.Literal(false)
            Token.Type.NUMBER, Token.Type.STRING -> Expr.Literal(next.literal)
            Token.Type.IDENTIFIER -> Expr.Variable(next)
            Token.Type.PAREN_L -> {
//                val expr = expression(cursor)
                cursor.expectNext(Token.Type.PAREN_R, "Expected ')' after expression.")
//                Expr.Grouping(expr)
                error("TODO")
            }
            else -> throw error(next, "Expected expression.")
        }
    }

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize(cursor: Cursor) {
        var current = cursor.next()
        while (!cursor.isEof()) {
            if (current.type == Token.Type.SEMICOLON) return
            if (cursor.peek().type in boundaryTypes) return
            current = cursor.next()
        }
    }

    private val boundaryTypes = setOf(
        Token.Type.CLASS, Token.Type.FUN, Token.Type.VAR,
        Token.Type.FOR, Token.Type.IF, Token.Type.WHILE,
        Token.Type.PRINT, Token.Type.RETURN
    )

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

        fun matchPeek(vararg types: Token.Type): Boolean {
            if (isEof()) return false
            val next = peek()
            return types.any { next.type == it }
        }

        fun matchNext(vararg types: Token.Type): Boolean {
            val next = peek()
            val matched = types.any { next.type == it }
            if (matched) pos++
            return matched
        }

        fun expectNext(type: Token.Type, message: String): Token {
            if (matchPeek(type)) return next()
            throw error(peek(), message)
        }

        fun isEof(): Boolean {
            return pos >= tokens.size || tokens[pos].type == Token.Type.EOF
        }
    }

    private class ParseError : RuntimeException()
}