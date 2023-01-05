plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("io.ktor.plugin") version "2.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    application
}

group = "org.waambokt"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.0")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.7.0")
    implementation("dev.kord:kord-core:0.8.0-M17")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("io.grpc:grpc-netty:1.51.0")
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("io.grpc:grpc-protobuf:1.51.1")
    implementation("io.grpc:grpc-core:1.51.1")
    implementation("io.netty:netty-codec:4.1.86.Final")
    implementation("io.grpc:grpc-api:1.51.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:20220924")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")
    implementation("org.litote.kmongo:kmongo-id:4.8.0")
    implementation("org.slf4j:slf4j-simple:2.0.5")

    implementation(project(":common"))
    implementation(project(":service-group:service-net"))
    implementation(project(":service-group:service-odds"))
    implementation(project(":service-group:service-score"))
    implementation(project(":service-group:service-cron"))
    implementation(project(":service-spec:service-spec-net"))
    implementation(project(":service-spec:service-spec-odds"))
    implementation(project(":service-spec:service-spec-score"))

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
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

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}
