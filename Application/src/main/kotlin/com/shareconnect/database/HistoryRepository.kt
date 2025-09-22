package com.shareconnect.database

import android.content.Context
import androidx.room.Room

class HistoryRepository(context: Context) {
    private val database: HistoryDatabase
    private val historyItemDao: HistoryItemDao

    init {
        // Initialize the database with SQLCipher encryption
        database = Room.databaseBuilder(
            context.applicationContext,
            HistoryDatabase::class.java, "history_database"
        )
            .allowMainThreadQueries() // For simplicity, allow main thread queries
            .build()
        historyItemDao = database.historyItemDao()
    }

    // Insert a new history item
    fun insertHistoryItem(item: HistoryItem) {
        historyItemDao.insert(item)
    }

    // Get all history items
    val allHistoryItems: List<HistoryItem>
        get() = historyItemDao.getAllHistoryItems()

    // Get history items by service provider
    fun getHistoryItemsByServiceProvider(serviceProvider: String): List<HistoryItem> {
        return historyItemDao.getHistoryItemsByServiceProvider(serviceProvider)
    }

    // Get history items by type
    fun getHistoryItemsByType(type: String): List<HistoryItem> {
        return historyItemDao.getHistoryItemsByType(type)
    }

    // Get history items by service type
    fun getHistoryItemsByServiceType(serviceType: String): List<HistoryItem> {
        return historyItemDao.getHistoryItemsByServiceType(serviceType)
    }

    // Get all service providers
    val allServiceProviders: List<String>
        get() = historyItemDao.getAllServiceProviders()

    // Get all types
    val allTypes: List<String>
        get() = historyItemDao.getAllTypes()

    // Get all service types
    val allServiceTypes: List<String>
        get() = historyItemDao.getAllServiceTypes()

    // Delete a specific history item
    fun deleteHistoryItem(item: HistoryItem) {
        historyItemDao.delete(item)
    }

    // Delete all history items
    fun deleteAllHistoryItems() {
        historyItemDao.deleteAll()
    }

    // Delete history items by service provider
    fun deleteHistoryItemsByServiceProvider(serviceProvider: String) {
        historyItemDao.deleteByServiceProvider(serviceProvider)
    }

    // Delete history items by type
    fun deleteHistoryItemsByType(type: String) {
        historyItemDao.deleteByType(type)
    }

    // Delete history items by service type
    fun deleteHistoryItemsByServiceType(serviceType: String) {
        historyItemDao.deleteByServiceType(serviceType)
    }
}