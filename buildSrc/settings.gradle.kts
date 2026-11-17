pluginManagement {
    repositories {
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-public")
        }
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}


buildscript {
    repositories {
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-public")
        }
        google()
        mavenCentral()
    }
}
