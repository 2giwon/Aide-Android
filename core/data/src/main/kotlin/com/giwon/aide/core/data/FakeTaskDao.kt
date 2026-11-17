package com.giwon.aide.core.data

import com.giwon.aide.core.database.TaskDao
import com.giwon.aide.core.database.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate

class FakeTaskDao : TaskDao {
    private val tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    private var nextId = 1L

    override fun observeOpenTasks(): Flow<List<TaskEntity>> =
        tasks.map { all ->
            all.filterNot {
                it.done
            }
        }


    override suspend fun insert(task: TaskEntity): Long {
        val id = nextId++
        tasks.update {
            it + task.copy(id = id)
        }
        return id
    }

    override suspend fun update(id: Long, title: String, due: LocalDate?) {
        tasks.update { all ->
            all.map {
                if (it.id == id) it.copy(title = title, due = due) else it
            }
        }
    }

    override suspend fun complete(id: Long, doneAt: Instant) {
        tasks.update { all ->
            all.map {
                if (it.id == id) it.copy(
                    done = true,
                    doneAt = doneAt
                ) else it
            }
        }
    }

    override suspend fun reopen(id: Long) {
        tasks.update { all ->
            all.map {
                if (it.id == id) it.copy(
                    done = false,
                    doneAt = null
                ) else it
            }
        }
    }

    override suspend fun delete(id: Long) {
        tasks.update { all -> all.filterNot { it.id == id } }
    }
}
