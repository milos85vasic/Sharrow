package com.metubeshare.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.shareconnect.database.HistoryDatabase;
import com.shareconnect.database.HistoryItem;
import com.shareconnect.database.HistoryItemDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class HistoryDatabaseTest {
    private HistoryDatabase database;
    private HistoryItemDao historyItemDao;
    
    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = androidx.room.Room.inMemoryDatabaseBuilder(context, HistoryDatabase.class)
                .allowMainThreadQueries()
                .build();
        historyItemDao = database.historyItemDao();
    }
    
    @After
    public void closeDb() throws IOException {
        database.close();
    }
    
    @Test
    public void testInsertAndRetrieve() throws Exception {
        // Create a test history item
        HistoryItem item = new HistoryItem();
        item.setUrl("https://youtube.com/watch?v=test123");
        item.setTitle("Test Video");
        item.setServiceProvider("YouTube");
        item.setType("single_video");
        item.setTimestamp(System.currentTimeMillis());
        item.setProfileId("profile1");
        item.setProfileName("Home Server");
        item.setSentSuccessfully(true);
        
        // Insert the item
        historyItemDao.insert(item);
        
        // Retrieve all items
        List<HistoryItem> items = historyItemDao.getAllHistoryItems();
        
        // Verify the item was inserted
        assertEquals(1, items.size());
        assertEquals("https://youtube.com/watch?v=test123", items.get(0).getUrl());
        assertEquals("Test Video", items.get(0).getTitle());
        assertEquals("YouTube", items.get(0).getServiceProvider());
        assertEquals("single_video", items.get(0).getType());
        assertEquals("profile1", items.get(0).getProfileId());
        assertEquals("Home Server", items.get(0).getProfileName());
        assertTrue(items.get(0).isSentSuccessfully());
    }
    
    @Test
    public void testDelete() throws Exception {
        // Create and insert a test history item
        HistoryItem item = new HistoryItem();
        item.setUrl("https://youtube.com/watch?v=test123");
        item.setTitle("Test Video");
        item.setServiceProvider("YouTube");
        item.setType("single_video");
        item.setTimestamp(System.currentTimeMillis());
        item.setProfileId("profile1");
        item.setProfileName("Home Server");
        item.setSentSuccessfully(true);
        
        historyItemDao.insert(item);
        
        // Verify item was inserted
        assertEquals(1, historyItemDao.getAllHistoryItems().size());
        
        // Delete the item
        historyItemDao.delete(item);
        
        // Verify item was deleted
        assertEquals(0, historyItemDao.getAllHistoryItems().size());
    }
}