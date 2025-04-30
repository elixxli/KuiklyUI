plugins {
    id("com.google.devtools.ksp") version(Version.getKSPVersion()) apply false
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath(BuildPlugin.kotlin)
        classpath(BuildPlugin.android)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
        jvmTargetValidationMode.set(org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode.WARNING)
    }
}