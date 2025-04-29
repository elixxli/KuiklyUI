import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.google.devtools.ksp")
}


group = Publishing.kuiklyGroup
version = "1.0.0"

repositories {
    mavenLocal()
}

kotlin {

    // target
    android() {
        publishLibraryVariantsGroupedByFlavor = true
        publishLibraryVariants("release")
    }

    jvm()

    ios()
    iosX64()
    iosSimulatorArm64()

    // sourceSet
    val commonMain by sourceSets.getting {
        dependencies {
            implementation(project(":core"))
            compileOnly(project(":core-annotations"))
        }
    }

    val androidMain by sourceSets.getting {
        dependsOn(commonMain)
//        kotlin.srcDirs(
//            "build/generated/ksp/android/androidDebug/kotlin",
//            "build/generated/ksp/android/androidRelease/kotlin",
//        )
    }

    val iosMain by sourceSets.getting {
        dependsOn(commonMain)
    }

    val jvmMain by sourceSets.getting {
        dependsOn(commonMain)
    }

    targets.withType<KotlinNativeTarget> {
        val mainSourceSets = this.compilations.getByName("main").defaultSourceSet
        when {
            konanTarget.family.isAppleFamily -> {
                binaries {
//                    framework {
//                        isStatic = true
//                        embedBitcode("bitcode")
//                        freeCompilerArgs = freeCompilerArgs + getCommonCompilerArgs()
//                    }
                }
                mainSourceSets.dependsOn(iosMain)
            }

            konanTarget.family == Family.ANDROID -> {
                binaries {
                    val outputName = "nativevue"
                    sharedLib(outputName, listOf(RELEASE)) {
                        linkerOpts += linkerOpts + getLinkerArgs()
                        freeCompilerArgs = freeCompilerArgs + getCommonCompilerArgs()
                    }
                    sharedLib(outputName, listOf(DEBUG)) {
                        freeCompilerArgs = freeCompilerArgs + getCommonCompilerArgs()
                    }
                }
            }
        }
    }

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
//        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
        }
        license = "MIT"
        extraSpecAttributes["resources"] = "['src/commonMain/assets/**']"
    }
}

ksp {
    arg("pageName", getPageName())
}

dependencies {
    compileOnly(project(":core-ksp")) {
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
        add("kspAndroid", this)
    }
}

android {
    compileSdk = 30
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }

//    buildTypes {
//        release {
//            ndk {
//                abiFilters.add("arm64-v8a")
//            }
//        }
//    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("src/androidMain/libs/")
            assets.srcDirs("src/commonMain/assets")
        }
    }

}

fun getPageName(): String {
    return project.properties["pageName"] as? String ?: ""
}

fun getCommonCompilerArgs(): List<String> {
    return listOf(
        "-Xallocator=std"
    )
}

fun getLinkerArgs(): List<String> {
    return listOf(
        "-Wl,--gc-sections,-s"
    )
}