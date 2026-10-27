package com.giwon.aide.core.calendar.di

import com.giwon.aide.core.calendar.CalendarDataSource
import com.giwon.aide.core.calendar.ContentResolverCalendarDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface CalendarModule {
    @Binds
    fun bindsCalendarDataSource(dataSource: ContentResolverCalendarDataSource): CalendarDataSource
}
