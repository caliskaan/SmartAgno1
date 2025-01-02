plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.agnoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.agnoapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.com.google.firebase.firebase.analytics)
    implementation(libs.firebase.messaging.v2310) // FCM SDK
    implementation(libs.google.firebase.analytics) //
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation (libs.okhttp)
    implementation (libs.java.websocket)
    implementation (libs.okhttp.v491)
    implementation (libs.gson)
    implementation(libs.activity)
    implementation (libs.java.websocket)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}