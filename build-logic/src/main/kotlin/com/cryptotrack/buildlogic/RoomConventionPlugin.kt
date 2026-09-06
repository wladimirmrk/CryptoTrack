package com.cryptotrack.buildlogic

import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Room-дополнение: применяется после cryptotrack.android.library.
 *
 * Внимание: androidx.room Gradle plugin НЕ используется — его вариантные артефакты
 * ломают резолв DAO-классов KSP/Dagger у потребителей (следствие ADR-015).
 * Схема экспортируется через ksp arg room.schemaLocation.
 */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<KspExtension> {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        dependencies {
            add("implementation", libs.library("androidx-room-runtime"))
            add("implementation", libs.library("androidx-room-ktx"))
            add("ksp", libs.library("androidx-room-compiler"))
        }
    }
}
