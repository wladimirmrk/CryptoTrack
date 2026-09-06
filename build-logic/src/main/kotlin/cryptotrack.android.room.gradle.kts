// Room-дополнение: применяется после cryptotrack.android.library.
import androidx.room.gradle.RoomExtension

plugins {
    id("androidx.room")
    id("com.google.devtools.ksp")
}

extensions.configure<RoomExtension> {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
