import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

/*
 * Reads the Google Maps API key from:
 * C:\Mobile Application\local.properties
 */
val localProperties = Properties().apply {
    val propertiesFile =
        rootProject.file("local.properties")

    if (propertiesFile.exists()) {
        propertiesFile
            .inputStream()
            .use { inputStream ->
                load(inputStream)
            }
    }
}

val mapsApiKey =
    localProperties
        .getProperty("MAPS_API_KEY")
        .orEmpty()
        .trim()

if (
    mapsApiKey.isBlank() ||
    mapsApiKey.startsWith("YOUR_")
) {
    throw GradleException(
        "MAPS_API_KEY is missing from local.properties"
    )
}

android {
    namespace = "com.example.movemate"
    compileSdk = 36

    defaultConfig {
        applicationId =
            "com.example.movemate"

        minSdk = 23
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        /*
         * Generates:
         * @string/google_maps_key
         */
        resValue(
            "string",
            "google_maps_key",
            mapsApiKey
        )
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(
        platform(libs.androidx.compose.bom)
    )
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    implementation(libs.google.maps)
    implementation(libs.maps.compose)

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"
    )

    // Current location and GPS updates
    implementation(
        "com.google.android.gms:play-services-location:21.3.0"
    )

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}