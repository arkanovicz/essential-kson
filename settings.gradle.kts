rootProject.name = "essential-kson"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android" || requested.id.name == "kotlin-android-extensions") {
                useModule("com.android.tools.build:gradle:4.0.1")
            }
            /*
            else when (requested.id.id) {
                // TODO factorize kotlin version
                "org.jetbrains.kotlin.multiplatform" -> "1.4.10"
            }
            */
        }
    }
}

