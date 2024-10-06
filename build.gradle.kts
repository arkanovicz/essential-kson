plugins {
    kotlin("multiplatform") version "1.9.25"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    signing
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.republicate.kson"
version = "2.4"

repositories {
    mavenCentral()
}

apply(plugin = "io.github.gradle-nexus.publish-plugin")
apply(plugin = "maven-publish")
apply(plugin = "signing")

kotlin {

    // explicitApi()
    jvm {
        compilations.all {
            // kotlin compiler compatibility options
            kotlinOptions {
                jvmTarget = "1.8"
                apiVersion = "1.9"
                languageVersion = "1.9"
            }
        }
    }
    js {
        browser {
            testTask {
                useKarma {
                    //useDebuggableChrome()
                    useChromeHeadless()
                    //useFirefox()
                    /*
                    webpackConfig.cssSupport {
                        enabled.set(true)
                    }*/
                }
            }
        }
        nodejs {
            testTask {

            }
        }
    }

     val hostOs = System.getProperty("os.name")
     val isMingwX64 = hostOs.startsWith("Windows")
     val nativeTarget = when {
//         hostOs == "Mac OS X" -> macosX64("native") TODO
         hostOs == "Linux" -> linuxX64("native")
         isMingwX64 -> mingwX64("native")
         else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
     }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                api("com.ionspin.kotlin:bignum:0.3.10")
                implementation("io.github.oshai:kotlin-logging:7.0.0")
            }
        }
        val commonTest by getting {
            dependencies {
                //implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("io.ktor:ktor-client-core:1.6.8")
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                runtimeOnly("org.slf4j:slf4j-simple:2.0.16")
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting
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
    named("kotlinNpmInstall").configure {
        onlyIf { false }
    }
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
