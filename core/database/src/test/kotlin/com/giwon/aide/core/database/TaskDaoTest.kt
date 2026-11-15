package com.giwon.aide.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.LocalDate


@RunWith(RobolectricTestRunner::class)
class TaskDaoTest {

    private lateinit var database: AideDatabase
    private lateinit var taskDao: TaskDao

    private val createdAt = Instant.parse("2026-10-12T01:00:00Z")

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AideDatabase::class.java,
        ).allowMainThreadQueries().build()

        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `삽입한 할일이 남은 할일에 노출된다`() = runTest {
        val id = taskDao.insert(
            task("보고서", due = LocalDate.of(2026, 10, 15))
        )

        assertThat(taskDao.observeOpenTasks().first()).containsExactly(
            task(
                "보고서",
                due = LocalDate.of(2026, 10, 15)
            ).copy(id = id)
        )

    }

    @Test
    fun `완료한 할일은 남은할일에서 빠진다`() = runTest {
        val id = taskDao.insert(task("보고서"))

        taskDao.complete(id, doneAt = Instant.parse("2026-10-12T02:00:00Z"))

        assertThat(taskDao.observeOpenTasks().first()).isEmpty()
    }

    @Test
    fun `지운 할일 제거`() = runTest {
        val id = taskDao.insert(task("보고서"))

        taskDao.delete(id)

        assertThat(taskDao.observeOpenTasks().first()).isEmpty()
    }

    private fun task(title: String, due: LocalDate? = null) = TaskEntity(
        title = title,
        due = due,
        done = false,
        createdAt = createdAt,
        doneAt = null
    )
}
