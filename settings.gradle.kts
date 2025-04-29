pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "KuiklyUI"

include(":core-annotations")
include(":core-ksp")

include(":core")
include(":core-render-android")
include(":demo")
include(":androidApp")

