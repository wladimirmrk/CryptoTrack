plugins {
    alias(libs.plugins.cryptotrack.android.library)
    alias(libs.plugins.cryptotrack.android.hilt)
}

android {
    namespace = "com.cryptotrack.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
