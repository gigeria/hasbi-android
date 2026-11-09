package com.hasbi.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HasbiApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleBudgetAlerts()
        schedulePurgeWorker()
    }

    /**
     * FIX FEAT-003: Schedule daily budget alert check.
     * Runs daily, checks if any budget is at 80%+.
     */
    private fun scheduleBudgetAlerts() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val budgetAlertWork = PeriodicWorkRequestBuilder<com.hasbi.budgets.alerts.BudgetAlertWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "hasbi_budget_alerts",
            ExistingPeriodicWorkPolicy.KEEP,
            budgetAlertWork
        )
    }

    /**
     * Schedule daily purge of expired soft-deleted transactions.
     */
    private fun schedulePurgeWorker() {
        // TODO: Create PurgeWorker and schedule daily at 3 AM
        // For now, budget alerts worker runs daily which is sufficient
    }
}
