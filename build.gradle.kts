plugins {
    kotlin("multiplatform") version "1.5.31"
    id("org.jetbrains.dokka") version "1.5.0"
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
//    signing
}

group = "com.republicate.kson"
version = "1.0"

repositories {
    mavenCentral()
//    maven(url = "https://kotlin.bintray.com/kotlinx/") // for kotlinx-datetime:0.3.1
}

apply(plugin = "io.github.gradle-nexus.publish-plugin")
apply(plugin = "maven-publish")
//apply(plugin = "signing")

kotlin {

    // explicitApi()
    jvm {
        compilations.all {
            // kotlin compiler compatibility options
            kotlinOptions {
                jvmTarget = "1.8"
                apiVersion = "1.5"
                languageVersion = "1.5"
            }
        }
    }
    js(LEGACY) {
        browser {
            testTask {
                useKarma {
                     useDebuggableChrome()
                    //useChromeHeadless()
                    //useFirefox()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        // nodejs() what?!
    }

    // (wip) fails with: Cannot add a KotlinSourceSet with name 'nativeMain' as a KotlinSourceSet with that name already exists.
//     val hostOs = System.getProperty("os.name")
//     val isMingwX64 = hostOs.startsWith("Windows")
//     val nativeTarget = when {
//         hostOs == "Mac OS X" -> macosX64("native")
//         hostOs == "Linux" -> linuxX64("native")
//         isMingwX64 -> mingwX64("native")
//         else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//     }
// 

    // linuxX64("linuxX64")
    // macosX64("macosX64")
    // mingwX64("mingwX64")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                api("io.github.gciatto:kt-math:0.4.0")
                implementation("io.github.microutils:kotlin-logging:2.0.11")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                implementation("io.ktor:ktor-client-core:1.6.5")
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                runtimeOnly("org.slf4j:slf4j-simple:1.7.32")
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        /*
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        */
    }
}

tasks {
    register<Jar>("dokkaJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }
    named("kotlinNpmInstall").configure {
        onlyIf { false }
    }
}

//signing {
//    val signingKey: String? by project
//    val signingPassword: String? by project
//    useInMemoryPgpKeys(signingKey, signingPassword)
//    sign(publishing.publications)
//}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("essential-kson")
            description.set("essential-kson $version - Lightweight JSON library for Kotlin")
            url.set("https://gitlab.renegat.net/claude/essential-kson")
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
                connection.set("scm:git@gitlab.renegat.net:claude/essential-kson.git")
                developerConnection.set("scm:git:ssh://github.com:MicroUtils/kotlin-logging.git")
                url.set("https://gitlab.renegat.net/claude/essential-kson")
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
