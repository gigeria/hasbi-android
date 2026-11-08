package com.hasbi.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.design.theme.HasbiTheme

/**
 * Onboarding navigation host — connects 3 onboarding screens.
 *
 * State machine:
 *   Step 0: Welcome
 *     - Get Started → Step 1
 *     - Skip → skipFromWelcome() → completeOnboarding() → onComplete()
 *   Step 1: Wallet Setup
 *     - Continue → Step 2
 *     - Skip → skipFromWalletSetup() → Step 2
 *     - Back → Step 0
 *   Step 2: Budget Setup
 *     - Start using Hasbi → completeOnboarding() → onComplete()
 *     - Skip → skipFromBudgetSetup() → completeOnboarding() → onComplete()
 *     - Back → Step 1
 *
 * When isComplete = true → calls onComplete() (App navigates to Dashboard)
 */
@Composable
fun OnboardingNavHost(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = HasbiTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error as snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }

    // Navigate to dashboard when complete
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when (state.currentStep) {
            0 -> WelcomeScreen(
                onGetStarted = { viewModel.nextStep() },
                onSkip = { viewModel.skipFromWelcome() }
            )
            1 -> WalletSetupScreen(
                selectedWallets = state.selectedWalletTypes,
                onToggleWallet = { viewModel.toggleWallet(it) },
                onContinue = { viewModel.nextStep() },
                onSkip = { viewModel.skipFromWalletSetup() },
                onBack = { viewModel.previousStep() }
            )
            2 -> BudgetSetupScreen(
                cycleType = state.cycleType,
                cycleStartDay = state.cycleStartDay,
                onCycleTypeChange = { viewModel.setCycleType(it) },
                onCycleStartDayChange = { viewModel.setCycleStartDay(it) },
                onBudgetChange = { viewModel.setMonthlyBudget(it) },
                onContinue = { viewModel.completeOnboarding() },
                onSkip = { viewModel.skipFromBudgetSetup() },
                onBack = { viewModel.previousStep() }
            )
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                containerColor = colors.textPrimary,
                contentColor = Color.White
            ) {
                Text(text = data.visuals.message)
            }
        }
    }
}
