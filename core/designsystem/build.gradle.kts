plugins {
    alias(libs.plugins.cryptotrack.android.library)
    alias(libs.plugins.cryptotrack.android.compose)
}

android {
    namespace = "com.cryptotrack.core.designsystem"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
