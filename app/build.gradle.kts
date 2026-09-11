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
        testInstrumentationRunner = "com.cryptotrack.app.HiltTestRunner"
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
    implementation(project(":feature:portfolio"))
    implementation(project(":feature:assetentry"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(libs.junit4)
    testImplementation(libs.konsist)

    androidTestImplementation(libs.junit4)
    // Version-less compose test artifacts resolve through the BOM (ComposeConventionPlugin
    // wires the platform only into implementation configurations).
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    // Empty test activity must live in the app's debug manifest: merged into the
    // androidTest apk instead, its process name (applicationId+.test) fails
    // InstrumentationActivityInvoker's same-process guard on launch.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.navigation.testing)
}
