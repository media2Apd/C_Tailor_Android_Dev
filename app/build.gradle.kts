@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)

    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.perf)   // replace this

}

android {
    namespace = "com.cuso.tailor"

    compileSdk = 37

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }

    defaultConfig {
        applicationId = "com.cuso.tailor"
        minSdk = 25
        //noinspection OldTargetApi
        targetSdk = 36

        //VERSION
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {

        debug {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"http://192.168.88.4:5000/\""
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://cuso-tailor-production.onrender.com/\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

//    tasks.matching { it.name == "preBuild" }.configureEach {
//        doFirst {
//            try {
//                Runtime.getRuntime().exec("adb reverse tcp:5000 tcp:5000")
//            } catch (_: Exception) { }
//        }
//    }
}

dependencies {

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3.lint)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.unit)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Splash
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.room3.common.jvm)

    // Google Auth
    //noinspection LoginCredentials
    implementation(libs.play.services.auth)

    implementation(libs.firebase.ai)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.volley)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Gson
    implementation(libs.gson)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Phone validation
    implementation(libs.libphonenumber)

    // Coil
    implementation(libs.coil.compose)

    // Charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // Unit tests
    testImplementation(libs.junit)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.accompanist.permissions)

    implementation(libs.colorpicker.compose)

    // iText7 for PDF generation
    implementation(libs.itext.kernel)
    implementation(libs.itext.io)
    implementation(libs.itext.layout)


    // For printing
    implementation(libs.androidx.print)

    //CRASHLYTICS

    // build.gradle.kts (app level)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    //PERFORMANCE


    // Add the dependency for the Performance Monitoring library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.perf)

    //UCrop for ProfileImageCrop
    implementation(libs.ucrop)
    implementation(libs.androidx.transition)

    //window app size class
    implementation (libs.androidx.compose.material3.window.size.class1)

    //QR barcode
    implementation(libs.core)
}