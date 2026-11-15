package com.giwon.aide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE done = 0")
    fun observeOpenTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query("UPDATE tasks SET title = :title, due = :due WHERE id = :id")
    suspend fun update(id: Long, title: String, due: LocalDate?)

    @Query("Update tasks SET done = 1, done_at = :doneAt WHERE id = :id")
    suspend fun complete(id: Long, doneAt: Instant)

    @Query("UPDATE tasks SET done = 0, done_at = NULL WHERE id = :id")
    suspend fun reopen(id: Long)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)
}
