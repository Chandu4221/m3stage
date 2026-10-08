plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "io.github.chandu4221"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinpoet)
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("io.github.chandu4221.m3stage.tooling.generator.MainKt")
}

tasks.test {
    useJUnitPlatform()
}