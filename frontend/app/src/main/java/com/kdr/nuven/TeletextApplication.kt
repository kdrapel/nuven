package com.kdr.nuven

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class TeletextApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicRefresh()
    }

    private fun schedulePeriodicRefresh() {
        val refreshWorkRequest = PeriodicWorkRequestBuilder<RefreshWorker>(4, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TeletextRefreshWork",
            ExistingPeriodicWorkPolicy.KEEP,
            refreshWorkRequest
        )
    }
}