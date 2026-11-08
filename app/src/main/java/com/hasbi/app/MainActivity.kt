package com.hasbi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.hasbi.app.navigation.HasbiNavHost
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Native SplashScreen API
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check onboarding status before showing content
        var isOnboarded: Boolean? = null
        splashScreen.setKeepOnScreenCondition { isOnboarded == null }

        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            isOnboarded = settings.isOnboarded

            setContent {
                HasbiTheme {
                    HasbiNavHost(isOnboarded = isOnboarded ?: false)
                }
            }
        }
    }
}
