package com.giwon.aide.feature.calendar.scheduler.data

import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import java.time.ZoneId

fun Event.toCalendarEvent(calendar: CalendarSource?): CalendarEvent {
    val ownerAccount = calendar?.ownerAccount.orEmpty()
    return CalendarEvent(
        id = "${id}_${start.toEpochMilli()}",
        calendarId = calendarId.toString(),
        title = title.ifEmpty { "(제목 없음)" },
        displayName = calendarName,
        startTime = start.toEpochMilli(),
        startDay = start.atZone(ZoneId.systemDefault()).dayOfMonth,
        endTime = end.toEpochMilli(),
        endDay = CalendarEvent.getEndDay(end.toEpochMilli(), allDay),
        color = color,
        isAllDay = allDay,
        ownerAccount = ownerAccount,
        isHoliday = ownerAccount.contains("samsungholiday"),
    )
}
