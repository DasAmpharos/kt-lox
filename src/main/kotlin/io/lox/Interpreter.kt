package io.lox

object Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach { it.accept(this) }
        } catch (e: RuntimeError) {
            TODO("report runtime error")
        }
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun executeBlock(
        statements: List<Stmt>,
        environment: Environment
    ) {
        val previousEnv = this.environment
        try {
            this.environment = environment
            statements.forEach { it.accept(this) }
        } finally {
            this.environment = previousEnv
        }
    }

    override fun visitAssign(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitBinary(expr: Expr.Binary): Any? {
        TODO("Not yet implemented")
    }

    override fun visitGrouping(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteral(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitLogical(expr: Expr.Logical): Any? {
        TODO("Not yet implemented")
    }

    override fun visitUnary(expr: Expr.Unary): Any? {
        TODO("Not yet implemented")
    }

    override fun visitVariable(expr: Expr.Variable): Any? {
        TODO("Not yet implemented")
    }

    override fun visitBlock(stmt: Stmt.Block) {
        TODO("Not yet implemented")
    }

    override fun visitExpression(stmt: Stmt.Expression) {
        TODO("Not yet implemented")
    }

    override fun visitIf(stmt: Stmt.If) {
        TODO("Not yet implemented")
    }

    override fun visitPrint(stmt: Stmt.Print) {
        TODO("Not yet implemented")
    }

    override fun visitVar(stmt: Stmt.Var) {
        TODO("Not yet implemented")
    }

    override fun visitWhile(stmt: Stmt.While) {
        TODO("Not yet implemented")
    }

    class Environment(
        private val enclosing: Environment? = null
    ) {
        private val values = HashMap<String, Any?>()

        fun define(name: String, value: Any?) {
            values[name] = value
        }

        operator fun get(name: Token): Any? {
            return when {
                values.containsKey(name.lexeme) -> values[name.lexeme]
                enclosing != null -> enclosing[name]
                else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
            }
        }

        fun assign(name: Token, value: Any?) {
            when {
                values.containsKey(name.lexeme) -> values[name.lexeme] = value
                enclosing != null -> enclosing.assign(name, value)
                else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
            }
        }
    }
}