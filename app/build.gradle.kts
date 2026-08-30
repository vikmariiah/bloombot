import java.util.Properties
import java.io.FileInputStream


val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(FileInputStream(localFile))
}



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "project.team36"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "project.team36"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "mapbox_access_token", localProperties.getProperty("MAPBOX_ACCESS_TOKEN", ""))
        resValue("string", "claude_api_key", localProperties.getProperty("CLAUDE_API_KEY", ""))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        resValues = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    //Navigation
    implementation(libs.androidx.navigation.compose)
    //MapBox
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.extension.compose)
    implementation(libs.mapbox.search)
    //ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    //Permissions
    implementation(libs.accompanist.permissions)
    //Serialization
    implementation(libs.kotlinx.serialization.json)
    // OkHttp for network and Server-Sent Events (SSE)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    // Coroutines for async calls
    implementation(libs.kotlinx.coroutines.android)
    //retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization.converter)
    //Icons
    implementation(libs.compose.material.icons.extended)
    // Ktor and SDK-client
    implementation(libs.mcp.kotlin.sdk)
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    //Coil, for pictures
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    //Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //Material3
    implementation(libs.material)

    //Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    //Room database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)


    //For tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}