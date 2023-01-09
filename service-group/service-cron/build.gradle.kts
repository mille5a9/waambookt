plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("io.ktor.plugin") version "2.1.2"
    id("com.google.protobuf") version "0.9.1"
    id("application")
}

group = "org.waambokt"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("it.skrape:skrapeit:1.3.0-alpha.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")
    implementation("com.google.protobuf:protobuf-java:3.21.7")
    implementation("io.grpc:grpc-api:1.51.1")
    implementation(project(":common"))
    implementation(project(":service-spec:service-spec-odds"))
    implementation(project(":service-spec:service-spec-score"))

    runtimeOnly("io.grpc:grpc-netty:1.51.0")
    runtimeOnly("io.grpc:grpc-core:1.51.1")
    runtimeOnly("org.litote.kmongo:kmongo-id:4.8.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.5")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("org.waambokt.service.cron.MainKt")
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}
