package com.hasbi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.hasbi.app.navigation.HasbiNavHost
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.repository.AppSettings
import com.hasbi.domain.repository.SettingsRepository
import com.hasbi.domain.repository.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var isReady = false
        var initialOnboarded = false
        var initialTheme = ThemeMode.SYSTEM
        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            initialOnboarded = settings.isOnboarded
            initialTheme = settings.themeMode
            isReady = true

            setContent {
                // Observe settings continuously — theme changes apply immediately
                val settingsState by settingsRepository.settings.collectAsState(initial = settings)
                
                val darkTheme = when (settingsState.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                }

                HasbiTheme(darkTheme = darkTheme) {
                    HasbiNavHost(isOnboarded = settingsState.isOnboarded)
                }
            }
        }
    }
}
