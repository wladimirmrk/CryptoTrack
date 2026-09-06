plugins {
    alias(libs.plugins.cryptotrack.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cryptotrack.core.navigation"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)
}
