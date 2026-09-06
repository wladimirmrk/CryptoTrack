package com.cryptotrack.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Базовая конфигурация android-library: Kotlin (built-in, AGP 9), compile/min SDK, detekt.
 * jvmTarget наследуется от compileOptions (AGP built-in Kotlin).
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        extensions.configure<LibraryExtension> {
            compileSdk = 37

            defaultConfig {
                minSdk = 26
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }

        configureDetekt()

        dependencies {
            add("detektPlugins", libs.library("detekt-formatting"))
        }
    }
}
