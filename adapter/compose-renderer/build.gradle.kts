plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}
group = "io.github.chandu4221"
version = "1.0.0"

dependencies {
    implementation(project(":domain"))
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)

    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    google()
}

tasks.test {
    useJUnitPlatform()
}