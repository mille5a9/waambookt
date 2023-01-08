plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("io.ktor.plugin") version "2.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("com.google.cloud.tools.jib") version "3.3.1"
    id("com.autonomousapps.dependency-analysis") version "1.18.0"
    application
}

group = "org.waambokt"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.kord:kord-core:0.8.0-M17")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("io.grpc:grpc-api:1.51.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:20220924")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")
    implementation("dev.kord:kord-common:0.8.0-M17")
    implementation("dev.kord:kord-gateway:0.8.0-M17")
    implementation("dev.kord:kord-rest:0.8.0-M17")
    implementation("io.ktor:ktor-client-core:2.2.1")
    implementation("io.ktor:ktor-http:2.2.1")
    implementation("io.ktor:ktor-utils:2.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    implementation(project(":common"))
    implementation(project(":service-group:service-net"))
    implementation(project(":service-group:service-odds"))
    implementation(project(":service-group:service-score"))
    implementation(project(":service-group:service-cron"))
    implementation(project(":service-spec:service-spec-net"))
    implementation(project(":service-spec:service-spec-odds"))
    implementation(project(":service-spec:service-spec-score"))

    runtimeOnly("org.litote.kmongo:kmongo-id:4.8.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.5")
    runtimeOnly("io.grpc:grpc-netty:1.51.0")
    runtimeOnly("io.grpc:grpc-core:1.51.1")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.5")
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    runtimeOnly("com.sksamuel.hoplite:hoplite-core:2.7.0")
    runtimeOnly("com.sksamuel.hoplite:hoplite-yaml:2.7.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("io.mockk:mockk:1.13.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    kotlinOptions.jvmTarget = "11"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    // Define the main class for the application.
    mainClass.set("org.waambokt.service.waambokt.MainKt")
}

if (System.getenv("ISPROD").toBoolean()) {
    jib.to.image = "localhost:5000/waambokt-prod"
} else {
    jib.to.image = "localhost:5000/waambokt-dev"
}
