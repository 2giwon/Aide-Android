package com.giwon.aide.core.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Instances
import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

internal class ContentResolverCalendarDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CalendarDataSource {

    private val contentResolver = context.contentResolver

    override fun observeChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        if (hasReadPermission()) {
            contentResolver.registerContentObserver(CalendarContract.CONTENT_URI, true, observer)
        }
        trySend(Unit)
        awaitClose { contentResolver.unregisterContentObserver(observer) }
    }.conflate()

    override suspend fun queryCalendars(): List<CalendarSource> =
        query(Calendars.CONTENT_URI, CALENDAR_PROJECTION) { it.toCalendarSource() }

    override suspend fun queryInstances(from: Instant, to: Instant, calendarIds: Set<Long>): List<Event> {
        if (calendarIds.isEmpty()) return emptyList()
        return query(
            uri = instancesUri(from, to),
            projection = INSTANCE_PROJECTION,
            selection = "${Instances.CALENDAR_ID} IN (${calendarIds.joinToString { "?" }})",
            selectionArgs = calendarIds.map(Long::toString).toTypedArray(),
            sortOrder = "${Instances.BEGIN} ASC",
        ) { it.toEvent() }
    }

    override suspend fun findInstance(eventId: Long, start: Instant): Event? =
        query(
            uri = instancesUri(start - ONE_DAY, start + ONE_DAY),
            projection = INSTANCE_PROJECTION,
            selection = "${Instances.EVENT_ID} = ? AND ${Instances.BEGIN} = ?",
            selectionArgs = arrayOf(eventId.toString(), start.toEpochMilli().toString()),
        ) { it.toEvent() }.firstOrNull()

    private suspend fun <T> query(
        uri: Uri,
        projection: Array<String>,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null,
        mapper: (Cursor) -> T,
    ): List<T> = withContext(Dispatchers.IO) {
        if (!hasReadPermission()) {
            Timber.w("READ_CALENDAR 권한이 없어 빈 목록을 돌려준다: %s", uri)
            return@withContext emptyList()
        }
        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            buildList { while (cursor.moveToNext()) add(mapper(cursor)) }
        }.orEmpty()
    }

    private fun hasReadPermission() =
        context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    private fun instancesUri(from: Instant, to: Instant): Uri =
        Instances.CONTENT_URI.buildUpon()
            .appendPath(from.toEpochMilli().toString())
            .appendPath(to.toEpochMilli().toString())
            .build()

    private fun Cursor.toCalendarSource() = CalendarSource(
        id = getLong(0),
        displayName = getString(1).orEmpty(),
        accountName = getString(2).orEmpty(),
        accountType = getString(3).orEmpty(),
        ownerAccount = getString(4).orEmpty(),
        color = getInt(5),
        writable = getInt(6) >= Calendars.CAL_ACCESS_CONTRIBUTOR,
    )

    private fun Cursor.toEvent() = Event(
        id = getLong(0),
        calendarId = getLong(1),
        title = getString(2).orEmpty(),
        start = Instant.ofEpochMilli(getLong(3)),
        end = Instant.ofEpochMilli(getLong(4)),
        allDay = getInt(5) == 1,
        color = getInt(6),
        calendarName = getString(7).orEmpty(),
    )

    private companion object {
        val ONE_DAY: Duration = Duration.ofDays(1)

        val CALENDAR_PROJECTION = arrayOf(
            Calendars._ID,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.ACCOUNT_NAME,
            Calendars.ACCOUNT_TYPE,
            Calendars.OWNER_ACCOUNT,
            Calendars.CALENDAR_COLOR,
            Calendars.CALENDAR_ACCESS_LEVEL,
        )

        val INSTANCE_PROJECTION = arrayOf(
            Instances.EVENT_ID,
            Instances.CALENDAR_ID,
            Instances.TITLE,
            Instances.BEGIN,
            Instances.END,
            Instances.ALL_DAY,
            Instances.DISPLAY_COLOR,
            Instances.CALENDAR_DISPLAY_NAME,
        )
    }
}
