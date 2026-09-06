package com.cryptotrack.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention для feature-модулей: library + compose + hilt.
 */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("cryptotrack.android.library")
        pluginManager.apply("cryptotrack.android.compose")
        pluginManager.apply("cryptotrack.android.hilt")
    }
}
