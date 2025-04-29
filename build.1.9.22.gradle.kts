plugins {
    kotlin("multiplatform") version "1.9.22" apply false
    id("com.android.application") version "7.4.2" apply false
    id("com.android.library") version "7.4.2" apply false
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}