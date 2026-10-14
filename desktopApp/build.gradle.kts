import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}


group = "io.github.chandu4221"
version = "1.0.0"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.compose.uiToolingPreview)

    // Explicitly add Material 3 for the desktop app UI
    implementation(libs.material.kolor)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    // Add your modules
    implementation(project(":domain"))
    implementation(project(":adapter:json-persistence"))
    implementation(project(":adapter:compose-codegen"))
    implementation(project(":adapter:compose-renderer"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnit()
    systemProperty("java.awt.headless", "true")
}

compose.desktop {
    application {
        mainClass = "io.github.chandu4221.m3stage.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "m3stage"
            packageVersion = "1.0.0"
        }
    }
}