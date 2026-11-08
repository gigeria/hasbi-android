package com.hasbi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hasbi.app.navigation.HasbiNavHost
import com.hasbi.design.theme.HasbiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Native SplashScreen API — installs before content is set
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO: Set isOnboarded check from DataStore (Week 4)
        // For Week 1: always show onboarding placeholder
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        setContent {
            HasbiTheme {
                HasbiNavHost(isOnboarded = false)
            }
        }

        // Simulate initialization completion
        isReady = true
    }
}
