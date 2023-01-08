plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "org.waambokt"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")

    api("org.litote.kmongo:kmongo-id:4.8.0")
    api("com.google.protobuf:protobuf-java:3.21.12")
    api("dev.kord:kord-common:0.8.0-M17")
    api("io.grpc:grpc-api:1.51.1")

    runtimeOnly("io.grpc:grpc-netty:1.51.0")
    runtimeOnly("io.grpc:grpc-core:1.51.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}
