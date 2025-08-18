package com.dls.pymetask

import android.app.Application
import android.os.Environment

import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel
import com.truelayer.payments.ui.TrueLayerUI
import dagger.hilt.android.HiltAndroidApp



@HiltAndroidApp
class PymeTaskApp : Application(){
    override fun onCreate() {
        super.onCreate()
        TrueLayerUI.init(this) {
          //  environment = Environment.SANDBOX
            httpConnection = HttpConnectionConfiguration(
                httpDebugLoggingLevel = HttpLoggingLevel.None
            )
        }
    }
}
