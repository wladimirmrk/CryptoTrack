package com.cryptotrack.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String) =
    findLibrary(alias).orElseThrow {
        IllegalArgumentException("Library '$alias' not found in version catalog")
    }

internal fun Project.configureDetekt() {
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        // A quality gate must fail, not rewrite sources: auto-correct silently
        // mutated files (and the embedded ktlint ruleset misfires on Kotlin 2.3).
        autoCorrect = false
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    }
}
