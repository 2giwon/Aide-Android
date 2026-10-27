package com.giwon.aide.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class VisibleCalendarStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /** 한 번도 고르지 않았으면 null */
    val calendarIds: Flow<Set<Long>?> = dataStore.data.map { preferences ->
        preferences[VISIBLE_CALENDAR_IDS]?.map(String::toLong)?.toSet()
    }

    suspend fun setCalendarIds(ids: Set<Long>) {
        dataStore.edit { it[VISIBLE_CALENDAR_IDS] = ids.map(Long::toString).toSet() }
    }

    private companion object {
        val VISIBLE_CALENDAR_IDS = stringSetPreferencesKey("visible_calendar_ids")
    }
}
