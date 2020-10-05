repositories {
    jcenter()
    mavenCentral()
    mavenLocal() // for kotlinx-io:0.2.0
}

plugins {
    kotlin("multiplatform") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.0"
    // id("com.jfrog.artifactory") version "4.17.2"
    // `maven-publish`
}

apply("versions.gradle.kts")

group = "com.republicate.json"
version = "1.0" + (if (System.getProperty("snapshot")?.toBoolean() == true) "-SNAPSHOT" else "")

// fails with message: Unresolved reference: compile
// dependencies {
//     compile("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
// }
// 

// same
// subprojects {
//     dependencies {
//         compile("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
//     }
// }
// 

dependencies {
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
}

kotlin {
    // explicitApi()
    jvm {
        compilations.all {
            // kotlin compiler compatibility options
            kotlinOptions {
                jvmTarget = "1.8"
                apiVersion = "1.8"
                languageVersion = "1.8"
            }
        }
    }
    js(LEGACY) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
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
                api("org.jetbrains.kotlinx:kotlinx-io:0.2.0")
                api("io.github.gciatto:kt-math:0.2.2")
                implementation("io.github.microutils:kotlin-logging:2.0.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                // implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16") should not be needed
                api("org.slf4j:slf4j-api:${extra["slf4j_version"]}")
            }
        }
        val jvmTest by getting {
            dependencies {
                // implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("junit:junit:${extra["junit_version"]}")
                /*
                implementation("org.mockito:mockito-all:${extra["mockito_version"]}")
                implementation("org.apache.logging.log4j:log4j-api:${extra["log4j_version"]}")
                implementation("org.apache.logging.log4j:log4j-core:${extra["log4j_version"]}")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:${extra["log4j_version"]}")
                 */
            }
        }
        val jsMain by getting {
            dependencies {
                // implementation("org.jetbrains.kotlinx:kotlinx-io-js:0.1.16") should not be needed
                api("org.slf4j:slf4j-api:${extra["slf4j_version"]}")
            }            
        }
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
}

/*
publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("essential-kson")
            description.set("essential-kson $version - Lightweight JSON library for Kotlin")
            //url.set("https://github.com/MicroUtils/kotlin-logging")
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    name.set("Claude Brisson")
                    email.set("claude.brisson@gmail.com")
                    organization.set("github")
                    organizationUrl.set("http://www.github.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/MicroUtils/kotlin-logging.git")
                developerConnection.set("scm:git:ssh://github.com:MicroUtils/kotlin-logging.git")
                url.set("http://github.com/MicroUtils/kotlin-logging/tree/master")
            }
        }
        artifact(tasks["dokkaJar"])
    }
}
*/

/*
publishing {
    val bintrayOrg = "microutils"
    val bintrayRepo = "kotlin-logging"
    val bintrayUser = System.getProperty("bintray.user")
    val bintrayApiKey = System.getProperty("bintray.key")

    if (bintrayUser != null && bintrayApiKey != null) {
        repositories {
            maven {
                name = "bintray"
                url = uri(
                    "https://api.bintray.com/maven/$bintrayOrg/$bintrayRepo/${project.name}/;publish=1;override=1"
                )
                credentials {
                    username = bintrayUser
                    password = bintrayApiKey
                }
            }
        }
    }
}
*/

//bintray {
//    user = System.getProperty("bintray.user")
//    key = System.getProperty("bintray.key") //https://bintray.com/profile/edit
//    setPublications(*publishing.publications.names.toTypedArray())
//    publish = true //[Default: false] Whether version should be auto published after an upload
//    pkg.apply {
//        repo = "kotlin-logging"
//        name = "kotlin-logging"
//        userOrg = "microutils"
//        setLicenses("Apache-2.0")
//        vcsUrl = "https://github.com/MicroUtils/kotlin-logging"
//        websiteUrl = "https://github.com/MicroUtils/kotlin-logging"
//        issueTrackerUrl = "https://github.com/MicroUtils/kotlin-logging/issues"
//
//        githubRepo = "MicroUtils/kotlin-logging"
//        githubReleaseNotesFile = "ChangeLog.md"
//        version.apply {
//            name = "${project.version}"
//            desc = "kotlin-logging - Lightweight logging framework for Kotlin"
//            released = "${Date()}"
//            gpg.sign = true //Determines whether to GPG sign the files. The default is false
//            mavenCentralSync.apply {
//                sync = true //[Default: true] Determines whether to sync the version to Maven Central.
//                user = System.getProperty("maven.user") //OSS user token: mandatory
//                password = System.getProperty("maven.password") //OSS user password: mandatory
//                close =
//                    "1" //Optional property. By default the staging repository is closed and artifacts are released to Maven Central. You can optionally turn this behaviour off (by puting 0 as value) and release the version manually.
//            }
//        }
//    }
//}

/*
artifactory {
    setContextUrl("http://oss.jfrog.org")
    publish(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig> {
        repository(delegateClosureOf<groovy.lang.GroovyObject> {
            setProperty("repoKey", "oss-snapshot-local")
            setProperty("username", System.getProperty("bintray.user"))
            setProperty("password", System.getProperty("bintray.key"))
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<groovy.lang.GroovyObject> {
            invokeMethod("publications", publishing.publications.names.toTypedArray())
            setProperty("publishArtifacts", true)
            setProperty("publishPom", true)
        })
    })
    resolve(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig> {
        setProperty("repoKey", "jcenter")
    })
    clientConfig.info.buildNumber = System.getProperty("build.number")
}
*/
