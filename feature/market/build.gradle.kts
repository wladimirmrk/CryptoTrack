plugins {
    alias(libs.plugins.cryptotrack.feature)
}

android {
    namespace = "com.cryptotrack.feature.market"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
}
