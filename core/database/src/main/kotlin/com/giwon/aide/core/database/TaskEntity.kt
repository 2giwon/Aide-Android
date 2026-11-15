package com.giwon.aide.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.giwon.aide.core.model.Task
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val due: LocalDate?,
    val done: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "done_at") val doneAt: Instant?
)

fun TaskEntity.asExternalModel() = Task(
    id = id,
    title = title,
    due = due,
    done = done,
    createdAt = createdAt,
    doneAt = doneAt
)
