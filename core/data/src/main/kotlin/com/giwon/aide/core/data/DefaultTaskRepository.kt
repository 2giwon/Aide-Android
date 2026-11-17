package com.giwon.aide.core.data

import com.giwon.aide.core.database.TaskDao
import com.giwon.aide.core.database.TaskEntity
import com.giwon.aide.core.database.asExternalModel
import com.giwon.aide.core.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

internal class DefaultTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val clock: Clock,
) : TaskRepository {

    override fun observeOpenTasks(): Flow<List<Task>> =
        taskDao.observeOpenTasks().map { tasks -> tasks.map { it.asExternalModel() } }

    override suspend fun add(title: String, due: LocalDate?): Long =
        taskDao.insert(
            TaskEntity(
                title = title,
                due = due,
                done = false,
                createdAt = clock.instant(),
                doneAt = null
            )
        )

    override suspend fun update(id: Long, title: String, due: LocalDate?) {
        taskDao.update(id, title, due)
    }

    override suspend fun complete(id: Long) {
        taskDao.complete(id, doneAt = clock.instant())
    }

    override suspend fun reopen(id: Long) {
        taskDao.reopen(id)
    }

    override suspend fun delete(id: Long) {
        taskDao.delete(id)
    }
}
