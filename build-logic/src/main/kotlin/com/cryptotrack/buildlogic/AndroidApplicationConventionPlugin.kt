package com.cryptotrack.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention для :app: application (built-in Kotlin, AGP 9) + detekt.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        extensions.configure<ApplicationExtension> {
            compileSdk = 37

            defaultConfig {
                minSdk = 26
                targetSdk = 37
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            buildTypes {
                release {
                    isMinifyEnabled = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        // Resolved against the applying module (:app is the only application).
                        "proguard-rules.pro",
                    )
                }
            }
        }

        configureDetekt()

        dependencies {
            add("detektPlugins", libs.library("detekt-formatting"))
        }
    }
}
