pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    // the catalog isn't usable for plugin versions in a build script's plugins block:
    // read the root kotlin version here so it stays the single source of truth
    val kotlinVersion = file("../gradle/libs.versions.toml").readLines()
        .first { it.trimStart().startsWith("kotlin ") || it.trimStart().startsWith("kotlin=") }
        .substringAfter('"').substringBefore('"')
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "essential-kson-benchmark"

// Substitutes com.republicate.kson:essential-kson with the local source tree
includeBuild("..")
