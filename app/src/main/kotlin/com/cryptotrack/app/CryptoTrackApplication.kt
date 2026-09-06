package com.cryptotrack.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt-точка входа приложения (требование @AndroidEntryPoint у MainActivity). */
@HiltAndroidApp
class CryptoTrackApplication : Application()
