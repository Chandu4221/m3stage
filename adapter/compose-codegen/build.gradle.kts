plugins {
    alias(libs.plugins.kotlinJvm)
}

group = "io.github.chandu4221"
version = "1.0.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.kotlinpoet)
    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}