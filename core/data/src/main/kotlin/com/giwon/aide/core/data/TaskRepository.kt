package com.giwon.aide.core.data

import com.giwon.aide.core.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    fun observeOpenTasks(): Flow<List<Task>>
    suspend fun add(title: String, due: LocalDate? = null): Long
    suspend fun update(id: Long, title: String, due: LocalDate?)
    suspend fun complete(id: Long)
    suspend fun reopen(id: Long)
    suspend fun delete(id: Long)
}
