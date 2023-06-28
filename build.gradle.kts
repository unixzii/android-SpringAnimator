// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        google()
    }
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
}
