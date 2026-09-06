plugins {
    alias(libs.plugins.cryptotrack.android.application)
    alias(libs.plugins.cryptotrack.android.compose)
    alias(libs.plugins.cryptotrack.android.hilt)
}

android {
    namespace = "com.cryptotrack.app"

    defaultConfig {
        applicationId = "com.cryptotrack.app"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
