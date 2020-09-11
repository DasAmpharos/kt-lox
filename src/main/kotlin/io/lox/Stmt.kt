package io.lox

import kotlin.collections.List

sealed class Stmt {
  abstract fun <R> accept(visitor: Visitor<R>): R

  data class Block(
    val statements: List<Stmt>
  ) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBlock(this)
  }

  data class Expression(
    val expression: Expr
  ) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitExpression(this)
  }

  data class If(
    val condition: Expr,
    val thenBranch: Stmt,
    val elseBranch: Stmt?
  ) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitIf(this)
  }

  data class Print(
    val expression: Expr
  ) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitPrint(this)
  }

  data class Var(
      val name: Token,
      val initializer: Expr?
  ) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitVar(this)
  }

  data class While(
    val condition: Expr,
    val body: Stmt
  ) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitWhile(this)
  }

  interface Visitor<R> {
    fun visitBlock(stmt: Block): R

    fun visitExpression(stmt: Expression): R

    fun visitIf(stmt: If): R

    fun visitPrint(stmt: Print): R

    fun visitVar(stmt: Var): R

    fun visitWhile(stmt: While): R
  }
}
