package com.cryptotrack.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/** Replaces the application with HiltTestApplication for instrumented tests. */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
