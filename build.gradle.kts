plugins {
    kotlin("multiplatform") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    signing
}

group = "com.republicate.kson"
version = "1.4"

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
                apiVersion = "1.5"
                languageVersion = "1.5"
            }
        }
    }
    js(IR) {
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

     val hostOs = System.getProperty("os.name")
     val isMingwX64 = hostOs.startsWith("Windows")
     val nativeTarget = when {
//         hostOs == "Mac OS X" -> macosX64("native")
         hostOs == "Linux" -> linuxX64("native")
         isMingwX64 -> mingwX64("native")
         else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
     }


    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                implementation("com.ionspin.kotlin:bignum:0.3.3")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation("io.ktor:ktor-client-core:1.6.7")
            }
        }
        val jvmMain by getting
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
        val nativeMain by getting
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
