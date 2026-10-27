package com.giwon.aide.core.model

data class CalendarSource(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val accountType: String,
    val ownerAccount: String,
    val color: Int,
    val writable: Boolean,
)
