plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.tastydiet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tastydiet"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // NDK configuration for llama.cpp
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        
        // External native build configuration
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
    }
    
    // NDK build configuration
    ndkVersion = "25.2.9519653"
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Optimize asset compression for large assets
            isDebuggable = true
        }
    }
    
    // Optimize asset processing for large files
    androidResources {
        noCompress += listOf("gguf", "bin", "mdl", "fst", "int", "conf", "stats", "dubm", "ie", "mat")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Room with KSP - Updated to latest stable version
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Compose - All versions aligned for compatibility
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.animation:animation:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // JNI and native library support
    implementation("com.alphacephei:vosk-android:0.3.47")
    
    // JSON processing for LLM responses
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // Charts for analytics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // PDF generation
    implementation("com.itextpdf:itext7-core:7.2.5")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
}

