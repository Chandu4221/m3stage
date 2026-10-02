plugins {
    alias(libs.plugins.kotlinJvm)
}

group = "io.github.chandu4221"
version = "1.0.0"


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}