dependencies {
    implementation(rootProject)
    implementation("com.squareup:kotlinpoet:1.6.0")
}

task("generateAst", JavaExec::class) {
    group = "generate"
    description = "Generate AST source files"

    main = "io.lox.tools.GenerateAstKt"
    classpath = sourceSets["main"].runtimeClasspath
    args(listOf(rootDir.path, "src", "main", "kotlin").joinToString(File.separator))
}
