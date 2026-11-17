package com.giwon.aide.core.database.di

import android.content.Context
import androidx.room.Room
import com.giwon.aide.core.database.AideDatabase
import com.giwon.aide.core.database.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesAideDatabase(@ApplicationContext context: Context): AideDatabase =
        Room.databaseBuilder(
            context,
            AideDatabase::class.java,
            "aide-database"
        ).build()

    @Provides
    fun providesTaskDao(database: AideDatabase): TaskDao = database.taskDao()
}
