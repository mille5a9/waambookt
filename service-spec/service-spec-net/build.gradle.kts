import com.google.protobuf.gradle.id

plugins {
    id("idea")
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.protobuf") version ("0.9.1")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
    }
}

dependencies {
    repositories {
        mavenCentral()
    }

    implementation("io.grpc:grpc-protobuf:1.51.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")

    api("com.google.guava:guava:31.1-android")
    api("com.google.protobuf:protobuf-java:3.21.11")
    api("io.grpc:grpc-api:1.51.1")
    api("io.grpc:grpc-kotlin-stub:1.2.1")
    api("io.grpc:grpc-stub:1.51.1")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/java")
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        kotlinOptions.jvmTarget = "11"
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.12.2"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.51.1"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.2.1:jdk7@jar"
        }
    }

    generatedFilesBaseDir = "$projectDir/build/generated/source/proto"

    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") { }
                id("grpckt") { }
            }
        }
    }
}
