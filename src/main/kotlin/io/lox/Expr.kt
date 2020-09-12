package io.lox

import kotlin.Any

sealed class Expr {
  abstract fun <R> accept(visitor: Visitor<R>): R

  data class Assign(
    val name: Token,
    val value: Expr
  ) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitAssign(this)
  }

  data class Binary(
    val op: Token,
    val left: Expr,
    val right: Expr
  ) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBinary(this)
  }

  data class Grouping(
    val expression: Expr
  ) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitGrouping(this)
  }

  data class Literal(
    val value: Any?
  ) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitLiteral(this)
  }

  data class Logical(
    val op: Token,
    val left: Expr,
    val right: Expr
  ) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitLogical(this)
  }

  data class Unary(
    val op: Token,
    val right: Expr
  ) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitUnary(this)
  }

  data class Variable(
    val name: Token
  ) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitVariable(this)
  }

  interface Visitor<R> {
    fun visitAssign(expr: Assign): R

    fun visitBinary(expr: Binary): R

    fun visitGrouping(expr: Grouping): R

    fun visitLiteral(expr: Literal): R

    fun visitLogical(expr: Logical): R

    fun visitUnary(expr: Unary): R

    fun visitVariable(expr: Variable): R
  }
}
