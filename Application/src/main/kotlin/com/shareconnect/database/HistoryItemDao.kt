package com.shareconnect.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface HistoryItemDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllHistoryItems(): List<HistoryItem>

    @Query("SELECT * FROM history_items WHERE serviceProvider = :serviceProvider ORDER BY timestamp DESC")
    fun getHistoryItemsByServiceProvider(serviceProvider: String): List<HistoryItem>

    @Query("SELECT * FROM history_items WHERE type = :type ORDER BY timestamp DESC")
    fun getHistoryItemsByType(type: String): List<HistoryItem>

    @Query("SELECT * FROM history_items WHERE serviceType = :serviceType ORDER BY timestamp DESC")
    fun getHistoryItemsByServiceType(serviceType: String): List<HistoryItem>

    @Query("SELECT * FROM history_items WHERE serviceProvider = :serviceProvider AND type = :type ORDER BY timestamp DESC")
    fun getHistoryItemsByServiceProviderAndType(serviceProvider: String, type: String): List<HistoryItem>

    @Query("SELECT DISTINCT serviceProvider FROM history_items")
    fun getAllServiceProviders(): List<String>

    @Query("SELECT DISTINCT type FROM history_items")
    fun getAllTypes(): List<String>

    @Query("SELECT DISTINCT serviceType FROM history_items")
    fun getAllServiceTypes(): List<String>

    @Insert
    fun insert(historyItem: HistoryItem)

    @Update
    fun update(historyItem: HistoryItem)

    @Delete
    fun delete(historyItem: HistoryItem)

    @Query("DELETE FROM history_items")
    fun deleteAll()

    @Query("DELETE FROM history_items WHERE serviceProvider = :serviceProvider")
    fun deleteByServiceProvider(serviceProvider: String)

    @Query("DELETE FROM history_items WHERE type = :type")
    fun deleteByType(type: String)

    @Query("DELETE FROM history_items WHERE serviceType = :serviceType")
    fun deleteByServiceType(serviceType: String)
}