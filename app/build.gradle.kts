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
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:market"))
    implementation(project(":feature:coin-details"))
    implementation(project(":feature:favorites"))
    implementation(project(":feature:search"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
}
