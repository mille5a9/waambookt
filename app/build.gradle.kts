/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.5.1/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("io.ktor.plugin") version "2.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("dev.kord:kord-core:0.8.0-M17")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")
    implementation("org.litote.kmongo:kmongo-id:4.8.0")
    implementation("org.slf4j:slf4j-simple:2.0.3")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("io.mockk:mockk:1.13.2")
}

tasks {
    // Use the native JUnit support of Gradle.
    "test"(Test::class) {
        useJUnitPlatform()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    kotlinOptions.jvmTarget = "11"
}

application {
    // Define the main class for the application.
    mainClass.set("waambokt.MainKt")
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}
