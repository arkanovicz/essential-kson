pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
          url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
    }
}

rootProject.name = "essential-kson"

