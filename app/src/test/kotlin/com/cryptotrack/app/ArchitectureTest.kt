package com.cryptotrack.app

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.fail
import org.junit.Test

/**
 * Architecture rules from docs/architecture.md §3, enforced via Konsist
 * (production sources only; build/generated directories are excluded by scope).
 */
class ArchitectureTest {
    private val scope = Konsist.scopeFromProject()

    private fun normalize(path: String): String = path.replace('\\', '/')

    private fun isFeatureFile(path: String): Boolean = "/feature/" in normalize(path)

    private fun featureName(path: String): String =
        // Module dir names use dashes (coin-details), package names do not (coindetails).
        normalize(path).substringAfter("/feature/").substringBefore("/").replace("-", "")

    private fun List<String>.assertNone(forbidden: List<String>, file: String) {
        filter { name -> forbidden.any(name::startsWith) }.forEach { name ->
            fail("$file: forbidden import '$name' (rule: presentation must not use Retrofit/OkHttp/Room/DataStore)")
        }
    }

    @Test
    fun `feature does not import another feature`() {
        scope.files
            .filter { isFeatureFile(it.path) }
            .forEach { file ->
                val ownPackage = "com.cryptotrack.feature.${featureName(file.path)}"
                file.imports
                    .filter { it.name.startsWith("com.cryptotrack.feature.") && !it.name.startsWith(ownPackage) }
                    .forEach { import ->
                        fail("${file.path}: feature imports another feature via '${import.name}'")
                    }
            }
    }

    @Test
    fun `core does not import feature`() {
        scope.files
            .filter { "/core/" in normalize(it.path) }
            .forEach { file ->
                file.imports
                    .filter { it.name.startsWith("com.cryptotrack.feature.") }
                    .forEach { import -> fail("${file.path}: core module imports feature via '${import.name}'") }
            }
    }

    @Test
    fun `core network does not import core data`() {
        scope.files
            .filter { "/core/network/" in normalize(it.path) }
            .forEach { file ->
                file.imports
                    .filter { it.name.startsWith("com.cryptotrack.core.data.") }
                    .forEach { import -> fail("${file.path}: core:network imports core:data via '${import.name}'") }
            }
    }

    @Test
    fun `presentation does not import infrastructure directly`() {
        val forbidden = listOf("retrofit2.", "okhttp3.", "androidx.room.", "androidx.datastore.")
        scope.files
            .filter { isFeatureFile(it.path) }
            .forEach { file ->
                file.imports.map { it.name }.assertNone(forbidden, file.path)
            }
    }

    @Test
    fun `domain does not import androidx except annotation`() {
        scope.files
            .filter { "/domain/" in normalize(it.path) }
            .forEach { file ->
                file.imports
                    .filter { it.name.startsWith("androidx.") && !it.name.startsWith("androidx.annotation.") }
                    .forEach { import -> fail("${file.path}: domain imports '${import.name}'") }
            }
    }

    @Test
    fun `view model does not import retrofit room or datastore`() {
        val forbidden = listOf("retrofit2.", "androidx.room.", "androidx.datastore.")
        scope.files
            .filter { it.name.endsWith("ViewModel.kt") }
            .forEach { file ->
                file.imports.map { it.name }.assertNone(forbidden, file.path)
            }
    }
}
