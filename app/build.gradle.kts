import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

// Load keystore properties at the top level
val keystorePropertiesFile = file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.fit.fitform"
    compileSdk = 36

    signingConfigs {
        // We only create the release config if the keystore file exists.
        // The 'debug' config is created automatically by the Android Gradle Plugin.
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.fit.fitform"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Assign the release signing config only if it was created
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            // The debug signing config is always available
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // --- Standard Android & Navigation ---
    implementation(libs.appcompat)
    implementation(libs.material) // Uses the version from your libs.versions.toml
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // --- ViewModel and LiveData ---
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // --- Room Database ---
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // --- Authentication ---
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // --- CameraX & Vision ---
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.vision.interfaces)
    implementation(libs.mlkit.pose.detection)
    implementation(libs.mlkit.pose.detection.accurate)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-storage")

    // --- Other Libraries ---
    implementation(libs.gson)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
