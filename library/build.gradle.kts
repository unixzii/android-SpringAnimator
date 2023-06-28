import java.io.ByteArrayOutputStream
plugins {
    id("com.android.library")
    id("maven-publish")
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

var gitVersionCode = 1
try {
    gitVersionCode = Integer.parseInt("git rev-list HEAD --count".runCommand())
} catch (ignored: NumberFormatException) {
    println("WARN: no git commits yet")
}

var gitVersionName = "git tag --list".runCommand().split('\n').first()
if (gitVersionName.isEmpty()) {
    gitVersionName = "1.0.0"
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 16
        targetSdk = 33
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.6.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "me.cyandev"
            artifactId = "springanimator"
            version = gitVersionName

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
