package com.seemoo.openflow

import android.app.Application
import android.content.Context
import com.seemoo.openflow.intents.IntentRegistry
import com.seemoo.openflow.intents.impl.DialIntent
import com.seemoo.openflow.intents.impl.EmailComposeIntent
import com.seemoo.openflow.intents.impl.ShareTextIntent
import com.seemoo.openflow.intents.impl.ViewUrlIntent
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class MyApplication : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Initialize Firebase Remote Config
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 1L else 3L
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        IntentRegistry.register(DialIntent())
        IntentRegistry.register(ViewUrlIntent())
        IntentRegistry.register(ShareTextIntent())
        IntentRegistry.register(EmailComposeIntent())
        IntentRegistry.init(this)
    }
}