import java.util.Properties

plugins {
    alias(libs.plugins.cryptotrack.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.cryptotrack.android.hilt)
}

android {
    namespace = "com.cryptotrack.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Ключ CoinGecko demo — необязателен для публичных эндпоинтов.
        // Задаётся в local.properties: coingeckoApiKey=..., либо переменной окружения COINGECKO_API_KEY.
        val localProperties = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use { load(it) }
        }
        val apiKey = localProperties.getProperty("coingeckoApiKey")
            ?: System.getenv("COINGECKO_API_KEY")
            ?: ""
        buildConfigField("String", "COINGECKO_API_KEY", "\"$apiKey\"")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
