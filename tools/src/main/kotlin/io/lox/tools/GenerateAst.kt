package io.lox.tools

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.lox.Token
import java.io.File
import kotlin.system.exitProcess

val expr = ClassName("io.lox", "Expr")
val stmt = ClassName("io.lox", "Stmt")
val returnType = TypeVariableName("R")

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }
    val outputDir = args[0]
    println(outputDir)

    val any = Any::class.asClassName()
    val token = Token::class.asClassName()
    defineAst(
        outputDir, expr, mapOf(
            "Assign" to mapOf(
                "name" to token,
                "value" to expr
            ),
            "Binary" to mapOf(
                "op" to token,
                "left" to expr,
                "right" to expr
            ),
            "Grouping" to mapOf(
                "expression" to expr
            ),
            "Literal" to mapOf(
                "value" to any.copy(nullable = true)
            ),
            "Logical" to mapOf(
                "op" to token,
                "left" to expr,
                "right" to expr
            ),
            "Unary" to mapOf(
                "op" to token,
                "right" to expr
            ),
            "Variable" to mapOf(
                "name" to token
            )
        )
    )

    defineAst(
        outputDir, stmt, mapOf(
            "Block" to mapOf(
                "statements" to List::class.asClassName().parameterizedBy(stmt)
            ),
            "Expression" to mapOf(
                "expression" to expr
            ),
            "If" to mapOf(
                "condition" to expr,
                "thenBranch" to stmt,
                "elseBranch" to stmt.copy(nullable = true)
            ),
            "Print" to mapOf(
                "expression" to expr
            ),
            "Var" to mapOf(
                "name" to token,
                "initializer" to expr.copy(nullable = true)
            ),
            "While" to mapOf(
                "condition" to expr,
                "body" to stmt
            )
        )
    )
}

private fun defineAst(
    outputDir: String,
    baseClassName: ClassName,
    types: Map<String, Map<String, TypeName>>
) {
    val visitor = baseClassName.nestedClass("Visitor").parameterizedBy(returnType)
    val fileSpec = FileSpec.builder(baseClassName.packageName, baseClassName.simpleName)
        .also { builder ->
            builder.addType(
                TypeSpec.classBuilder(baseClassName)
                    .addModifiers(KModifier.SEALED)
                    .addFunction(
                        FunSpec.builder("accept")
                            .addModifiers(KModifier.ABSTRACT)
                            .addTypeVariable(returnType)
                            .addParameter("visitor", visitor)
                            .returns(returnType)
                            .build()
                    )
                    .defineSubClasses(baseClassName, types, visitor)
                    .defineVisitor(baseClassName, types.keys)
                    .build()
            )
        }
        .build()

    val file = File(outputDir)
    file.mkdir()
    fileSpec.writeTo(file)
}

fun TypeSpec.Builder.defineSubClasses(
    baseClassName: ClassName,
    types: Map<String, Map<String, TypeName>>,
    visitor: ParameterizedTypeName,
): TypeSpec.Builder {
    types.forEach { (typeName, typeFields) ->
        defineType(typeName, typeFields, baseClassName, visitor)
    }
    return this
}

fun TypeSpec.Builder.defineType(
    className: String,
    fields: Map<String, TypeName>,
    baseClassName: ClassName,
    visitor: ParameterizedTypeName
): TypeSpec.Builder {
    return addType(
        TypeSpec.classBuilder(className)
            .superclass(baseClassName)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .also { ctorBuilder ->
                        fields.forEach { (fieldName, fieldType) ->
                            ctorBuilder.addParameter(fieldName, fieldType)
                        }
                    }
                    .build()
            )
            .also { typeBuilder ->
                fields.forEach { (fieldName, fieldType) ->
                    typeBuilder.addProperty(
                        PropertySpec.builder(fieldName, fieldType)
                            .initializer(fieldName)
                            .build()
                    )
                }
            }
            .addFunction(
                FunSpec.builder("accept")
                    .addModifiers(KModifier.OVERRIDE)
                    .addTypeVariable(returnType)
                    .addParameter("visitor", visitor)
                    .addStatement("return visitor.visit${className.capitalize()}(this)")
                    .returns(returnType)
                    .build()
            )
            .build()
    )
}

fun TypeSpec.Builder.defineVisitor(
    baseClassName: ClassName,
    types: Set<String>
): TypeSpec.Builder {
    val visitor = baseClassName.nestedClass("Visitor")
        .parameterizedBy(returnType)
    return addType(
        TypeSpec.interfaceBuilder(visitor.rawType.simpleName)
            .addTypeVariable(returnType)
            .also { builder ->
                types.forEach { type ->
                    val typeClassName = baseClassName.nestedClass(type)
                    builder.addFunction(
                        FunSpec.builder("visit${type.capitalize()}")
                            .addModifiers(KModifier.ABSTRACT)
                            .addParameter(baseClassName.simpleName.decapitalize(), typeClassName)
                            .returns(returnType)
                            .build()
                    )
                }
            }
            .build()
    )
}