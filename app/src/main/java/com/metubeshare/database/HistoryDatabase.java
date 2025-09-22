package com.metubeshare.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {HistoryItem.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class HistoryDatabase extends RoomDatabase {
    public abstract HistoryItemDao historyItemDao();
}