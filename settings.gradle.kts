pluginManagement {
    repositories {
        maven { url = uri("https://plugins.gradle.org/m2/") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.3.1"
        id("com.android.library") version "8.3.1"
        id("org.jetbrains.kotlin.android") version "1.9.21"
        id("com.google.devtools.ksp") version "1.9.21-1.0.15"
        kotlin("jvm") version "1.9.21"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "TastyDiet"
include(":app")
 