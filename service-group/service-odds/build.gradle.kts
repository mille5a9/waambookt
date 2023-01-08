plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("com.google.protobuf") version "0.9.1"
    id("io.ktor.plugin") version "2.1.2"
    id("application")
}

group = "org.waambokt"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:3.21.7")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("io.grpc:grpc-api:1.51.1")
    implementation("io.ktor:ktor-client-core-jvm:2.1.3")
    implementation("io.ktor:ktor-http:2.1.3")
    implementation("io.ktor:ktor-utils:2.1.3")
    implementation("org.json:json:20220924")
    implementation("org.litote.kmongo:kmongo-async-shared:4.8.0")
    implementation("org.litote.kmongo:kmongo-coroutine-core:4.8.0")
    implementation("org.litote.kmongo:kmongo-property:4.8.0")
    implementation("org.litote.kmongo:kmongo-shared:4.8.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")
    implementation("org.litote.kmongo:kmongo-id:4.8.0")
    implementation("org.mongodb:bson:4.8.0")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.8.0")

    implementation(project(":common"))
    implementation(project(":service-spec:service-spec-odds"))
    implementation(project(":service-spec:service-spec-score"))

    runtimeOnly("io.grpc:grpc-netty:1.51.0")
    runtimeOnly("io.grpc:grpc-core:1.51.1")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.5")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("org.waambokt.service.odds.MainKt")
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}
