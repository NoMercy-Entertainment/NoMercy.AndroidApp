import org.gradle.kotlin.dsl.invoke

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    namespace = "tv.nomercy.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "tv.nomercy.app"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CAST_APP_ID", "\"925B4C3C\"")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "SUFFIX", "\"\"")
        }
        debug {
            buildConfigField("String", "SUFFIX", "\"-dev\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

configurations.all {
    exclude(group = "xmlpull", module = "xmlpull")
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Material 3 dependency - needed for Material 3 themes
    implementation(libs.material.v1110)

    // Jetpack Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Networking - Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // JSON parsing
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    // OAuth2/OIDC Authentication (instead of Keycloak Android SDK)
    implementation(libs.appauth)
    implementation(libs.androidx.browser)

    // Secure storage for tokens
    implementation(libs.androidx.security.crypto)

    implementation(libs.core)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.navigation.safe.args.generator)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.test)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.lifecycle.process)

    // Debug tools
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.composemeshgradient)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil3.coil.svg)

    implementation(libs.androidx.media)

    // Google Cast (sender + Cast Connect TV receiver)
    implementation(libs.androidx.mediarouter)
    implementation(libs.gms.play.services.cast.framework)
    implementation(libs.gms.play.services.cast.tv)
    implementation(libs.play.services.cast)

}
