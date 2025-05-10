plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // Plugin Google Services
}

android {
    namespace = "com.kostify.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kostify.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    // UI & Layout
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Core
    implementation(libs.firebase.firestore)
    implementation (libs.play.services.auth.v2100)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)

    // Google Sign-In (jika digunakan)
    implementation(libs.googleid)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.play.services.auth)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation (libs.appcompat.v161)
}
