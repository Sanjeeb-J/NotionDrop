package com.sanjeeb.notiondrop.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sanjeeb.notiondrop.worker.WidgetSyncWorker

class WidgetCaptureAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val text = parameters[ActionParameters.Key<String>("captured_text")] ?: return
        
        val workData = Data.Builder()
            .putString("captured_text", text)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<WidgetSyncWorker>()
            .setInputData(workData)
            .build()
            
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
