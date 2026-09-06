// Hilt-дополнение: применяется после cryptotrack.android.library / cryptotrack.android.application.
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
