plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("com.google.protobuf") version "0.9.1"
    id("application")
}

group = "org.waambokt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-netty:1.51.0")
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("io.grpc:grpc-protobuf:1.51.1")
    implementation("io.grpc:grpc-core:1.51.1")
    implementation("io.netty:netty-codec:4.1.86.Final")
    implementation("javax.inject:javax.inject:1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation(project(":service-spec:service-spec-net"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("org.waambokt.service.net.MainKt")
}
