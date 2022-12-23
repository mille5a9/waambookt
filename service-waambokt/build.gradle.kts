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
    implementation("dev.kord:kord-core:0.8.0-M17")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.slf4j:slf4j-simple:2.0.5")

    implementation(project(":common"))
    implementation(project(":service-spec:service-spec-net"))

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