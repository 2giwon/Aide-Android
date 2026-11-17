package com.giwon.aide.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DefaultTaskRepositoryTest {

    private val now = Instant.parse("2026-10-12T01:00:00Z")
    private val repository = DefaultTaskRepository(
        taskDao = FakeTaskDao(),
        clock = Clock.fixed(now, ZoneOffset.UTC),
    )

    @Test
    fun `할 일 추가시 현재 시각으로 추가된다`() = runTest {
        val id = repository.add("할 일")

        val task = repository.observeOpenTasks().first().single()
        assertThat(task.id).isEqualTo(id)
        assertThat(task.title).isEqualTo("할 일")
        assertThat(task.due).isNull()
        assertThat(task.done).isFalse()
        assertThat(task.createdAt).isEqualTo(now)
    }

    @Test
    fun `완료된 할일은 할일 리스트에서 제거된다`() = runTest {
        val id = repository.add("완료된 할일")

        repository.complete(id)

        assertThat(repository.observeOpenTasks().first()).isEmpty()
    }

    @Test
    fun `완료를 취소하면 다시 할 일 리스트에 들어가고 완료 시각이 지워진다`() = runTest {
        val id = repository.add("보고서")
        repository.complete(id)

        repository.reopen(id)

        val task = repository.observeOpenTasks().first().single()
        assertThat(task.done).isFalse()
        assertThat(task.doneAt).isNull()
    }

    @Test
    fun `제목과 마감일을 고친다`() = runTest {
        val id = repository.add("보고서")

        repository.update(id, "보고서 v2", LocalDate.of(2026, 10, 15))

        val task = repository.observeOpenTasks().first().single()
        assertThat(task.title).isEqualTo("보고서 v2")
        assertThat(task.due).isEqualTo(LocalDate.of(2026, 10, 15))
    }

    @Test
    fun `지운 할 일은 사라진다`() = runTest {
        val id = repository.add("보고서")

        repository.delete(id)

        assertThat(repository.observeOpenTasks().first()).isEmpty()
    }

    @Test
    fun `없는 할 일을 완료시 무시`() = runTest {
        repository.add("보고서")

        repository.complete(999)

        assertThat(repository.observeOpenTasks().first()).extracting<String> { it.title }.containsExactly("보고서")
    }
}
