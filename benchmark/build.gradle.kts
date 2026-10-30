plugins {
    kotlin("jvm")
    id("me.champeau.jmh") version "0.7.4"
}

group = "com.republicate.kson"
version = "benchmark"

kotlin {
    jvmToolchain(17)
}

dependencies {
    // substituted by the included build (../) -> local jvm artifact
    jmhImplementation("com.republicate.kson:essential-kson:2.16")
    jmhImplementation("com.google.code.gson:gson:2.14.0")
    jmhImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.1")
    // kotlin-logging (transitive from kson) hard-requires an slf4j binding at class init
    jmhRuntimeOnly("org.slf4j:slf4j-nop:${libs.versions.slf4j.get()}")
}

// -Pjmh.<x> overrides, e.g. ../gradlew jmh -Pjmh.warmupIterations=1 -Pjmh.iterations=2
fun opt(name: String, default: String) = (findProperty("jmh.$name") ?: default).toString()

jmh {
    jmhVersion = "1.37"
    fork = opt("fork", "1").toInt()
    warmupIterations = opt("warmupIterations", "3").toInt()
    warmup = opt("warmup", "2s")
    iterations = opt("iterations", "5").toInt()
    timeOnIteration = opt("timeOnIteration", "2s")
    benchmarkMode = listOf("thrpt")
    timeUnit = "ms"
    resultFormat = "TEXT"
    // dot, not comma, as decimal separator in the report (the runner JVM otherwise inherits the shell locale)
    environment.put("JAVA_TOOL_OPTIONS", "-Duser.language.format=en -Duser.country.format=US")
    findProperty("jmh.includes")?.let { includes = listOf(it.toString()) }
}
