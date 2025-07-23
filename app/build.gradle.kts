// ═══════════════════════════════════════════════════════════════════════════════════
// Application Level build.gradle.kts - Main app module configuration
// libs.versions.toml file থেকে dependencies এবং plugins ব্যবহার করা
// ═══════════════════════════════════════════════════════════════════════════════════

plugins {
    // Version catalog থেকে plugins apply করা
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.nidoham.stream"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.nidoham.stream"
        minSdk = 26
        targetSdk = 35
        versionCode = libs.versions.version.code.get().toInt()
        versionName = libs.versions.version.name.get()
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // KSP configuration
        ksp {
            arg("dagger.hilt.shareTestComponents", "true")
        }
    }
    
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
    
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.version.get().toInt())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.version.get().toInt())
    }
    
    kotlinOptions {
        jvmTarget = libs.versions.java.version.get()
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Core Android & Kotlin - Version catalog থেকে individual libraries
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // AndroidX UI Components - Bundle ব্যবহার করে একসাথে add করা
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.bundles.androidx.ui)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Architecture Components - Bundle ব্যবহার করা
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.bundles.androidx.architecture)
    implementation(libs.bundles.androidx.navigation)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Dependency Injection - Individual libraries
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Network & API - Bundle ব্যবহার করা
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.bundles.network)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Asynchronous Programming - Bundles ব্যবহার করা
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.rxjava)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Media & Image Processing - Bundle এবং individual libraries
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.bundles.media3)
    implementation(libs.glide)
    ksp(libs.glide.ksp)
    implementation(libs.circle.imageview)
    implementation(libs.lottie)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Utility Libraries - Individual libraries
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.gson)
    implementation(libs.prettytime)
    implementation(libs.timber)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Third-party Specialized - Individual libraries
    // ═══════════════════════════════════════════════════════════════════════════════════
    implementation(libs.newpipe.extractor)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Development & Debugging - Debug build এর জন্য
    // ═══════════════════════════════════════════════════════════════════════════════════
    debugImplementation(libs.leakcanary)
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // Testing - Bundles ব্যবহার করা
    // ═══════════════════════════════════════════════════════════════════════════════════
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}