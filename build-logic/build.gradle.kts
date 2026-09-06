plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "cryptotrack.android.application"
            implementationClass = "com.cryptotrack.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "cryptotrack.android.library"
            implementationClass = "com.cryptotrack.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "cryptotrack.android.compose"
            implementationClass = "com.cryptotrack.buildlogic.ComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "cryptotrack.android.hilt"
            implementationClass = "com.cryptotrack.buildlogic.HiltConventionPlugin"
        }
        register("androidRoom") {
            id = "cryptotrack.android.room"
            implementationClass = "com.cryptotrack.buildlogic.RoomConventionPlugin"
        }
        register("feature") {
            id = "cryptotrack.feature"
            implementationClass = "com.cryptotrack.buildlogic.FeatureConventionPlugin"
        }
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.composeCompiler.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
}
