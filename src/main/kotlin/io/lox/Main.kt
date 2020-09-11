package io.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: kotlang [script]")
        exitProcess(64)
    }

    val runner = when (args.size) {
        1 -> FileRunner(args[0])
        else -> PromptRunner
    }
    runner.run()
}

interface Runner {
    fun run()
}

class FileRunner(
    private val filename: String
) : Runner {
    private var hadError = false

    override fun run() {
        val bytes = Files.readAllBytes(Paths.get(filename))
        val source = String(bytes, Charset.defaultCharset())

        val tokens = Scanner.scanTokens(source)
        if (hadError) exitProcess(65)
        println(tokens)
    }
}

object PromptRunner : Runner {
    override fun run() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            val source = reader.readLine() ?: break
            val tokens = Scanner.scanTokens(source)
//            val statements = Parser.parse(tokens)
//            Interpreter.interpret(statements)
            println(tokens)

            if (Lox.hadError) exitProcess(65)
            if (Lox.hadRuntimeError) exitProcess(70)
        }
    }
}