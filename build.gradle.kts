import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.21"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    // johnjenglema
    id("com.gradleup.shadow") version "9.2.2"
}

group = "io.github.facticee"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    shadow("dev.jorel:commandapi-kotlin-paper:11.0.0")
    shadow("dev.jorel:commandapi-paper-shade:11.0.0")
    shadow(kotlin("stdlib"))
    shadow(files("libraries/my-textlib-1.0.0.jar"))
}

tasks {
    // Konfiguriere den ShadowJar-Task
    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.getByName("shadow"))
        relocate("dev.jorel.commandapi", "io.github.facticee.libs.commandapi")
        relocate("com.example.textlib", "io.github.facticee.libs.textlib")
        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    assemble {
        dependsOn(named("shadowJar"))
    }

    build {
        dependsOn(named("shadowJar"))
    }
}


kotlin {
    jvmToolchain(23)

    tasks {
        compileKotlin {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_23)
        }
        compileJava {
            options.encoding = "UTF-8"
            options.release.set(23)
        }
    }
}
