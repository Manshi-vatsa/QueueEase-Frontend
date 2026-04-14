plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val backendBaseUrlOverride = (project.findProperty("BACKEND_BASE_URL") as String?) ?: ""
val backendPort = (project.findProperty("BACKEND_PORT") as String?) ?: "8080"
val physicalDeviceHost = (project.findProperty("BACKEND_DEVICE_HOST") as String?) ?: "10.22.174.212"
val emulatorHost = (project.findProperty("BACKEND_EMULATOR_HOST") as String?) ?: "10.0.2.2"

android {
    namespace = "com.masterapp.queueeaseapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.masterapp.queueeaseapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrlOverride\"")
        buildConfigField("String", "BACKEND_PORT", "\"$backendPort\"")
        buildConfigField("String", "BACKEND_DEVICE_HOST", "\"$physicalDeviceHost\"")
        buildConfigField("String", "BACKEND_EMULATOR_HOST", "\"$emulatorHost\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.compose.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
}