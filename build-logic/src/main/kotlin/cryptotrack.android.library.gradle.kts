// Convention для android-library модулей (core:*).
plugins {
    id("cryptotrack.kotlin.android")
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}
