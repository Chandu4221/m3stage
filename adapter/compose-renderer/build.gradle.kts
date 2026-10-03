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

    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}