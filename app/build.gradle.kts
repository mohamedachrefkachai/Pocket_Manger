plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.recyclersleam"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.recyclersleam"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.biometric)
    implementation(libs.android.mail)
    implementation(libs.android.activation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.room:room-runtime:2.8.2")
    annotationProcessor("androidx.room:room-compiler:2.8.2")
    implementation(libs.mpAndroidChart)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Carte OpenStreetMap (Osmdroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")
}