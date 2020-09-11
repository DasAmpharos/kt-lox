plugins {
    kotlin("jvm") version "1.4.10"
}

group = "io.github.dylmeadows"
version = "0.0.1"

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))
    }
}