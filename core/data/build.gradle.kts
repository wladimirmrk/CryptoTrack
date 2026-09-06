plugins {
    alias(libs.plugins.cryptotrack.android.library)
    alias(libs.plugins.cryptotrack.android.hilt)
    alias(libs.plugins.cryptotrack.android.room)
}

android {
    namespace = "com.cryptotrack.core.data"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))

    implementation(project(":core:datastore"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
