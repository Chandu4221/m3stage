plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.chandu4221"
version = "1.0.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnit()
}