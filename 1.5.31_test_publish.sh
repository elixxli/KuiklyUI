java --version
KUIKLY_KOTLIN_VERSION="1.5.31" ./gradlew -c settings.1.5.31.gradle.kts :core:publishToMavenLocal --stacktrace
KUIKLY_KOTLIN_VERSION="1.5.31" ./gradlew -c settings.1.5.31.gradle.kts :core-annotations:publishToMavenLocal --stacktrace
KUIKLY_KOTLIN_VERSION="1.5.31" ./gradlew -c settings.1.5.31.gradle.kts :core-ksp:publishToMavenLocal --stacktrace
KUIKLY_KOTLIN_VERSION="1.5.31" ./gradlew -c settings.1.5.31.gradle.kts :core-render-android:publishToMavenLocal --stacktrace