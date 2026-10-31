@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    signing
    alias(libs.plugins.versions)
}

group = "com.republicate.kson"
version = "2.18"

kotlin {

    applyDefaultHierarchyTemplate {
        common {
            group("jvmCommon") {
                withCompilations { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }
            }
        }
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
    }

    // explicitApi()
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
    android {
        namespace = "com.republicate.kson"
        compileSdk = 35
        minSdk = 21
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        withHostTest { }
    }
    js {
        browser {
            testTask {
                useKarma {
                    useDebuggableChrome()
                    //useChromeHeadless()
                    // useFirefox()
                    /*
                    webpackConfig.cssSupport {
                        enabled.set(true)
                    }*/
                }
            }
        }
        nodejs {
            testTask {
                useMocha { timeout = "10s" }  // increase 2s default, flaky
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    linuxArm64()
    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()
    macosX64()
    macosArm64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    mingwX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.datetime)
                api(libs.bignum)
                implementation(libs.kotlin.logging)
                // Force proper version of serialization-core to fix JS classpath resolution
                implementation(libs.kotlinx.serialization.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.coroutines.test)
                // implementation(libs.ktor)
            }
        }
        val jvmCommonMain by getting {
            dependencies {
                // kotlin-logging-jvm declares slf4j-api compileOnly; without it consumers crash at first use
                implementation(libs.slf4j.api)
            }
        }
        val jvmCommonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                runtimeOnly(libs.slf4j)
            }
        }
        val webMain by getting
        val webTest by getting {
            dependencies {
                implementation(libs.ktor)
            }
        }
        val jsMain by getting
        val jsTest by getting
        val wasmJsMain by getting
        val wasmJsTest by getting
        val wasmWasiTest by getting {
            dependencies {
                implementation(libs.kotlinx.io)
            }
        }
        all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

tasks {
    // The compiler-emitted WASI runner preopens nothing, so the tests see no filesystem. No KGP hook exists;
    // Kotlin 2.4.20 adds `wasmWasi { wasmtime() }` (KT-86633), whose runtime takes preopens as `--dir` arguments:
    // check whether its test task exposes them, then drop this patch.
    val patchWasmWasiTestRunner by registering {
        dependsOn("compileTestDevelopmentExecutableKotlinWasmWasi")
        // Capture project root at configuration time for configuration cache compatibility
        val projectRoot = layout.projectDirectory.asFile.absolutePath.replace("\\", "/")
        val mjsFilePath = layout.buildDirectory.file("compileSync/wasmWasi/test/testDevelopmentExecutable/kotlin/essential-kson-test.mjs")
        doLast {
            val mjsFile = mjsFilePath.get().asFile
            if (mjsFile.exists()) {
                val content = mjsFile.readText()
                val patched = content.replace(
                    "const wasi = new WASI({ version: 'preview1', args: argv, env, });",
                    "const wasi = new WASI({ version: 'preview1', args: argv, env, preopens: { '.': '$projectRoot' } });"
                )
                mjsFile.writeText(patched)
            }
        }
    }
    named("wasmWasiNodeTest") {
        dependsOn(patchWasmWasiTestRunner)
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    // only the Central upload needs signatures; publishToMavenLocal feeds scratch consumers
    if (gradle.startParameter.taskNames.any { it.contains("MavenCentral") }) signAllPublications()
    pom {
        name.set("essential-kson")
        description.set("essential-kson $version - Lightweight JSON library for Kotlin")
        url.set("https://github.com/arkanovicz/essential-kson")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("cbrisson")
                name.set("Claude Brisson")
                email.set("claude.brisson@gmail.com")
                organization.set("republicate.com")
                organizationUrl.set("https://republicate.com")
            }
        }
        scm {
            connection.set("scm:git@github.com:arkanovicz/essential-kson.git")
            url.set("https://github.com/arkanovicz/essential-kson")
        }
    }
}

signing {
    useGpgCmd()
}
