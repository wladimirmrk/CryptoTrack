package com.cryptotrack.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.PackageInfoCompat

/** Версия приложения для строки «CryptoTrack Version» (макет: «v4.2.1 (Build 8904)»). */
@Composable
internal fun rememberAppVersionText(): String {
    val context = LocalContext.current
    return remember(context) {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        val build = PackageInfoCompat.getLongVersionCode(info)
        "v${info.versionName} (Build $build)"
    }
}
