import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    `maven-publish`
    alias(libs.plugins.nexusPublish)
    signing
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.republicate.kson"
version = "2.5"

kotlin {

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        JvmPlatforms.jvmPlatformByTargetVersion(JvmTarget.JVM_17)
    }

    // explicitApi()
    jvm()
    js {
        browser {
            testTask {
                useKarma {
                    //useDebuggableChrome()
                    useChromeHeadless()
                    // useFirefox()
                    /*
                    webpackConfig.cssSupport {
                        enabled.set(true)
                    }*/
                }
            }
        }
        nodejs()
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
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    // watchosArm32()
    watchosArm64()
    // watchosDeviceArm64()
    watchosX64()
    watchosSimulatorArm64()
    mingwX64()
    /* waiting for kotlinx-datetime 0.6.2 and ktor 3.0.0
    wasmJs {
        browser()
    }
    wasmWasi()
    */
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.datetime)
                api(libs.bignum)
                implementation(libs.kotlin.logging)
            }
        }
        val commonTest by getting {
            dependencies {
                //implementation(kotlin("test"))
                implementation(libs.kotlin.test)
                // implementation(kotlin("test-common"))
                // implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.coroutines)
                // implementation(libs.ktor)
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                runtimeOnly(libs.slf4j)
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(libs.ktor)
            }
        }
        /*
        val wasmJsTest by getting {
            dependencies {
                //implementation(kotlin(Deps.WasmJs.test))
                //implementation(libs.kotlin.test.js)
            }
        }
         */
        all {
            // languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.optIn("expect-actual-classes")
            // languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
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
    register<Jar>("dokkaJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }
    /*
    named("kotlinNpmInstall").configure {
        onlyIf { false }
    }
    */
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("essential-kson")
            description.set("essential-kson $version - Lightweight JSON library for Kotlin")
            url.set("https://gitlab.republicate.com/claude/essential-kson")
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    name.set("Claude Brisson")
                    email.set("claude.brisson@gmail.com")
                    organization.set("republicate.com")
                    organizationUrl.set("https://republicate.com")
                }
            }
            scm {
                connection.set("scm:git@gitlab.republicate.com:claude/essential-kson.git")
                url.set("https://gitlab.republicate.com/claude/essential-kson")
            }
        }
        artifact(tasks["dokkaJar"])
    }
}

nexusPublishing {
    repositories {
        sonatype {
            useStaging.set(true)
        }
    }
}
