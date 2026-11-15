package com.giwon.aide.core.model

import java.time.Instant
import java.time.LocalDate

data class Task(
    val id: Long,
    val title: String,
    val due: LocalDate?,
    val done: Boolean,
    val createdAt: Instant,
    val doneAt: Instant?
)
