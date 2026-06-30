package com.sanjeeb.notiondrop.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.sanjeeb.notiondrop.data.local.HistoryDao
import com.sanjeeb.notiondrop.data.remote.StructuredContent
import com.sanjeeb.notiondrop.repository.ContentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyDao: HistoryDao,
    private val contentRepository: ContentRepository,
    private val gson: Gson
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val queuedEntries = historyDao.getQueuedEntries()
            var allSuccess = true
            
            for (entry in queuedEntries) {
                try {
                    val structuredContent = gson.fromJson(entry.content, StructuredContent::class.java)
                    contentRepository.sendToNotion(structuredContent)
                    // If successful, delete the queued one (sendToNotion creates a new SENT one)
                    historyDao.deleteEntry(entry)
                } catch (e: Exception) {
                    allSuccess = false
                    // Update entry to failed or keep as queued based on policy
                    // For now, keep as queued to retry later
                }
            }

            if (allSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
