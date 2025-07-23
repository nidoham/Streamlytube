// ═══════════════════════════════════════════════════════════════════════════════════
// Project Level build.gradle.kts - Root project configuration
// libs.versions.toml file ব্যবহার করে modern version catalog approach
// ═══════════════════════════════════════════════════════════════════════════════════

plugins {
    // Version catalog থেকে plugins apply করা
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.versions) apply false
}

// Clean task - build artifacts remove করার জন্য
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
    delete(rootProject.layout.buildDirectory)
}