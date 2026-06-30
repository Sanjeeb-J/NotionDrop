package com.sanjeeb.notiondrop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // e.g. note, task, syllabus, etc.
    val content: String,
    val targetDatabaseName: String,
    val timestamp: Long,
    val status: String // "SENT", "QUEUED", "FAILED"
)
