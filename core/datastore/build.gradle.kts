plugins {
    alias(libs.plugins.cryptotrack.android.library)
    alias(libs.plugins.cryptotrack.android.hilt)
}

android {
    namespace = "com.cryptotrack.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
