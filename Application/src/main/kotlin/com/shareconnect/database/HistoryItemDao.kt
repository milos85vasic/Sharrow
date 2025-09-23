package com.shareconnect.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface HistoryItemDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllHistoryItems(): List<HistoryItem>

    @Query("SELECT * FROM history_items WHERE id = :id")
    fun getHistoryItemById(id: Int): HistoryItem?

    @Query("SELECT * FROM history_items WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getHistoryItemsByProfile(profileId: String): List<HistoryItem>

    @Query("SELECT * FROM history_items WHERE isSentSuccessfully = 1 ORDER BY timestamp DESC")
    fun getSuccessfulHistoryItems(): List<HistoryItem>

    @Query("SELECT * FROM history_items WHERE isSentSuccessfully = 0 ORDER BY timestamp DESC")
    fun getFailedHistoryItems(): List<HistoryItem>

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
    fun insert(historyItem: HistoryItem): Long

    @Insert
    fun insertHistoryItem(historyItem: HistoryItem): Long

    @Update
    fun update(historyItem: HistoryItem)

    @Update
    fun updateHistoryItem(historyItem: HistoryItem)

    @Delete
    fun delete(historyItem: HistoryItem)

    @Delete
    fun deleteHistoryItem(historyItem: HistoryItem)

    @Query("DELETE FROM history_items")
    fun deleteAll()

    @Query("DELETE FROM history_items")
    fun deleteAllHistoryItems()

    @Query("DELETE FROM history_items WHERE serviceProvider = :serviceProvider")
    fun deleteByServiceProvider(serviceProvider: String)

    @Query("DELETE FROM history_items WHERE type = :type")
    fun deleteByType(type: String)

    @Query("DELETE FROM history_items WHERE serviceType = :serviceType")
    fun deleteByServiceType(serviceType: String)
}