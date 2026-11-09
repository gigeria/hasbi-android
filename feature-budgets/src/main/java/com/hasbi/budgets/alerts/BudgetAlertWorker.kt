package com.hasbi.budgets.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hasbi.domain.repository.BudgetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Daily budget alert worker — checks if any budget is at 80%+ and shows notification.
 *
 * Runs daily via WorkManager.
 */
@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val budgetRepository: BudgetRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val cycle = budgetRepository.getCurrentCycle() ?: return Result.success()
        val budgets = budgetRepository.observeBudgetsWithSpent(cycle.id, cycle.startDate, cycle.endDate)

        // Collect latest values
        var hasAlert = false
        budgets.collect { list ->
            for (budget in list) {
                if (budget.isWarning && !budget.isOverBudget) {
                    showNotification(
                        "Budget Alert",
                        "Aap ka budget ${budget.percentageUsed.toInt()}% tak use ho gaya hai."
                    )
                    hasAlert = true
                    break
                }
                if (budget.isOverBudget) {
                    showNotification(
                        "Budget Exceeded",
                        "Aap ka budget exceed ho gaya hai. Aaj Ka Baqi 0 hai."
                    )
                    hasAlert = true
                    break
                }
            }
            return@collect
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hasbi_budget_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

// Extension for percentageUsed
val com.hasbi.database.relation.BudgetWithSpent.percentageUsed: Float
    get() = if (limit > 0) (spent.toFloat() / limit.toFloat()) * 100 else 0f
