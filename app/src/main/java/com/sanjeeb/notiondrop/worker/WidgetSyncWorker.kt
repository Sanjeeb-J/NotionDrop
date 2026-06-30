package com.sanjeeb.notiondrop.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sanjeeb.notiondrop.repository.ContentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WidgetSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentRepository: ContentRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val text = inputData.getString("captured_text") ?: return@withContext Result.failure()

        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "notiondrop_sync_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Widget Sync",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        try {
            val structuredContent = contentRepository.processContent(text)
            contentRepository.sendToNotion(structuredContent)
            
            val notification = NotificationCompat.Builder(appContext, channelId)
                .setContentTitle("NotionDrop")
                .setContentText("Successfully sent: ${structuredContent.title}")
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .build()
                
            notificationManager.notify(1, notification)
            Result.success()
        } catch (e: Exception) {
            val notification = NotificationCompat.Builder(appContext, channelId)
                .setContentTitle("NotionDrop Failed")
                .setContentText("Could not send to Notion: ${e.message}")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build()
                
            notificationManager.notify(2, notification)
            Result.failure()
        }
    }
}
