plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

ksp {
    arg("room.generateKotlin", "true")
}

android {
    namespace = "com.example.nextwatch"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.nextwatch"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    // Retrofit core
    implementation(libs.retrofit)


    // JSON converter (Gson) — parses API responses into your data classes
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.coil)
    // implementation(libs.coil.transformations)

    // OkHttp — Retrofit uses this under the hood for networking
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    implementation("com.google.android.exoplayer:exoplayer:2.17.0")
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}