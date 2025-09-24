package com.shareconnect.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HistoryItem::class, Theme::class, ServerProfileEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyItemDao(): HistoryItemDao
    abstract fun themeDao(): ThemeDao
    abstract fun serverProfileDao(): ServerProfileDao
}