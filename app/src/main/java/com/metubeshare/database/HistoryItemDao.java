package com.metubeshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface HistoryItemDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    List<HistoryItem> getAllHistoryItems();
    
    @Query("SELECT * FROM history_items WHERE serviceProvider = :serviceProvider ORDER BY timestamp DESC")
    List<HistoryItem> getHistoryItemsByServiceProvider(String serviceProvider);
    
    @Query("SELECT * FROM history_items WHERE type = :type ORDER BY timestamp DESC")
    List<HistoryItem> getHistoryItemsByType(String type);
    
    @Query("SELECT * FROM history_items WHERE profileId = :profileId ORDER BY timestamp DESC")
    List<HistoryItem> getHistoryItemsByProfileId(String profileId);
    
    @Query("SELECT * FROM history_items WHERE serviceProvider = :serviceProvider AND type = :type ORDER BY timestamp DESC")
    List<HistoryItem> getHistoryItemsByServiceProviderAndType(String serviceProvider, String type);
    
    @Query("SELECT DISTINCT serviceProvider FROM history_items")
    List<String> getAllServiceProviders();
    
    @Query("SELECT DISTINCT type FROM history_items")
    List<String> getAllTypes();
    
    @Insert
    void insert(HistoryItem historyItem);
    
    @Update
    void update(HistoryItem historyItem);
    
    @Delete
    void delete(HistoryItem historyItem);
    
    @Query("DELETE FROM history_items")
    void deleteAll();
    
    @Query("DELETE FROM history_items WHERE serviceProvider = :serviceProvider")
    void deleteByServiceProvider(String serviceProvider);
    
    @Query("DELETE FROM history_items WHERE type = :type")
    void deleteByType(String type);
    
    @Query("DELETE FROM history_items WHERE profileId = :profileId")
    void deleteByProfileId(String profileId);
}