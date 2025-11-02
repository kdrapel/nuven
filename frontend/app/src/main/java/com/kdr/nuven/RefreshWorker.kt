package com.kdr.nuven

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RefreshWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val updated = DataManager.refreshData()
            if (updated) {
                DataManager.notifyDataChanged()
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
