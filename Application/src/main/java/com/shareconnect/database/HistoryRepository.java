package com.shareconnect.database;

import android.content.Context;
import androidx.room.Room;
import java.util.List;

public class HistoryRepository {
    private HistoryDatabase database;
    private HistoryItemDao historyItemDao;
    
    public HistoryRepository(Context context) {
        // Initialize the database with SQLCipher encryption
        database = Room.databaseBuilder(context.getApplicationContext(),
                HistoryDatabase.class, "history_database")
                .allowMainThreadQueries() // For simplicity, allow main thread queries
                .build();
        historyItemDao = database.historyItemDao();
    }
    
    // Insert a new history item
    public void insertHistoryItem(HistoryItem item) {
        historyItemDao.insert(item);
    }
    
    // Get all history items
    public List<HistoryItem> getAllHistoryItems() {
        return historyItemDao.getAllHistoryItems();
    }
    
    // Get history items by service provider
    public List<HistoryItem> getHistoryItemsByServiceProvider(String serviceProvider) {
        return historyItemDao.getHistoryItemsByServiceProvider(serviceProvider);
    }
    
    // Get history items by type
    public List<HistoryItem> getHistoryItemsByType(String type) {
        return historyItemDao.getHistoryItemsByType(type);
    }
    
    // Get history items by service type
    public List<HistoryItem> getHistoryItemsByServiceType(String serviceType) {
        return historyItemDao.getHistoryItemsByServiceType(serviceType);
    }
    
    // Get all service providers
    public List<String> getAllServiceProviders() {
        return historyItemDao.getAllServiceProviders();
    }
    
    // Get all types
    public List<String> getAllTypes() {
        return historyItemDao.getAllTypes();
    }
    
    // Get all service types
    public List<String> getAllServiceTypes() {
        return historyItemDao.getAllServiceTypes();
    }
    
    // Delete a specific history item
    public void deleteHistoryItem(HistoryItem item) {
        historyItemDao.delete(item);
    }
    
    // Delete all history items
    public void deleteAllHistoryItems() {
        historyItemDao.deleteAll();
    }
    
    // Delete history items by service provider
    public void deleteHistoryItemsByServiceProvider(String serviceProvider) {
        historyItemDao.deleteByServiceProvider(serviceProvider);
    }
    
    // Delete history items by type
    public void deleteHistoryItemsByType(String type) {
        historyItemDao.deleteByType(type);
    }
    
    // Delete history items by service type
    public void deleteHistoryItemsByServiceType(String serviceType) {
        historyItemDao.deleteByServiceType(serviceType);
    }
}