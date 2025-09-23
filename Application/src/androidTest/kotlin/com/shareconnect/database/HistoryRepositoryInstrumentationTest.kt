package com.shareconnect.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryRepositoryInstrumentationTest {

    private lateinit var database: HistoryDatabase
    private lateinit var historyDao: HistoryItemDao
    private lateinit var historyRepository: HistoryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()

        historyDao = database.historyItemDao()
        historyRepository = HistoryRepository(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveHistoryItem() {
        val historyItem = HistoryItem().apply {
            url = "https://www.youtube.com/watch?v=test"
            title = "Test Video"
            serviceProvider = "YouTube"
            type = "video"
            timestamp = System.currentTimeMillis()
            profileId = "profile-123"
            profileName = "Test Profile"
            isSentSuccessfully = true
            serviceType = "metube"
        }

        val insertedId = historyDao.insertHistoryItem(historyItem)
        assertTrue(insertedId > 0)

        val retrievedItem = historyDao.getHistoryItemById(insertedId.toInt())
        assertNotNull(retrievedItem)
        assertEquals(historyItem.url, retrievedItem?.url)
        assertEquals(historyItem.title, retrievedItem?.title)
        assertEquals(historyItem.serviceProvider, retrievedItem?.serviceProvider)
        assertEquals(historyItem.profileId, retrievedItem?.profileId)
        assertEquals(historyItem.isSentSuccessfully, retrievedItem?.isSentSuccessfully)
    }

    @Test
    fun testGetAllHistoryItems() {
        val items = listOf(
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=test1"
                title = "Test Video 1"
                serviceProvider = "YouTube"
                timestamp = System.currentTimeMillis()
                isSentSuccessfully = true
            },
            HistoryItem().apply {
                url = "https://vimeo.com/test2"
                title = "Test Video 2"
                serviceProvider = "Vimeo"
                timestamp = System.currentTimeMillis()
                isSentSuccessfully = false
            }
        )

        items.forEach { historyDao.insertHistoryItem(it) }

        val allItems = historyDao.getAllHistoryItems()
        assertEquals(2, allItems.size)
        assertTrue(allItems.any { it.serviceProvider == "YouTube" })
        assertTrue(allItems.any { it.serviceProvider == "Vimeo" })
    }

    @Test
    fun testGetHistoryItemsByProfile() {
        val testProfileId = "test-profile"

        val items = listOf(
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=test1"
                title = "Test Video 1"
                profileId = testProfileId
                profileName = "Test Profile"
                timestamp = System.currentTimeMillis()
            },
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=test2"
                title = "Test Video 2"
                profileId = "other-profile"
                profileName = "Other Profile"
                timestamp = System.currentTimeMillis()
            }
        )

        items.forEach { historyDao.insertHistoryItem(it) }

        val profileItems = historyDao.getHistoryItemsByProfile(testProfileId)
        assertEquals(1, profileItems.size)
        assertEquals("Test Video 1", profileItems[0].title)
        assertEquals(testProfileId, profileItems[0].profileId)
    }

    @Test
    fun testGetSuccessfulHistoryItems() {
        val items = listOf(
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=success"
                title = "Successful Video"
                isSentSuccessfully = true
                timestamp = System.currentTimeMillis()
            },
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=failed"
                title = "Failed Video"
                isSentSuccessfully = false
                timestamp = System.currentTimeMillis()
            }
        )

        items.forEach { historyDao.insertHistoryItem(it) }

        val successfulItems = historyDao.getSuccessfulHistoryItems()
        assertEquals(1, successfulItems.size)
        assertEquals("Successful Video", successfulItems[0].title)
        assertTrue(successfulItems[0].isSentSuccessfully)
    }

    @Test
    fun testGetFailedHistoryItems() {
        val items = listOf(
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=success"
                title = "Successful Video"
                isSentSuccessfully = true
                timestamp = System.currentTimeMillis()
            },
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=failed"
                title = "Failed Video"
                isSentSuccessfully = false
                timestamp = System.currentTimeMillis()
            }
        )

        items.forEach { historyDao.insertHistoryItem(it) }

        val failedItems = historyDao.getFailedHistoryItems()
        assertEquals(1, failedItems.size)
        assertEquals("Failed Video", failedItems[0].title)
        assertFalse(failedItems[0].isSentSuccessfully)
    }

    @Test
    fun testUpdateHistoryItem() {
        val historyItem = HistoryItem().apply {
            url = "https://www.youtube.com/watch?v=test"
            title = "Original Title"
            isSentSuccessfully = false
            timestamp = System.currentTimeMillis()
        }

        val insertedId = historyDao.insertHistoryItem(historyItem)
        historyItem.id = insertedId.toInt()

        val updatedItem = historyItem.apply {
            title = "Updated Title"
            isSentSuccessfully = true
        }

        historyDao.updateHistoryItem(updatedItem)

        val retrievedItem = historyDao.getHistoryItemById(insertedId.toInt())
        assertNotNull(retrievedItem)
        assertEquals("Updated Title", retrievedItem?.title)
        assertTrue(retrievedItem?.isSentSuccessfully == true)
    }

    @Test
    fun testDeleteHistoryItem() {
        val historyItem = HistoryItem().apply {
            url = "https://www.youtube.com/watch?v=test"
            title = "Item to Delete"
            timestamp = System.currentTimeMillis()
        }

        val insertedId = historyDao.insertHistoryItem(historyItem)
        historyItem.id = insertedId.toInt()

        val insertedItem = historyDao.getHistoryItemById(insertedId.toInt())
        assertNotNull(insertedItem)

        historyDao.deleteHistoryItem(historyItem)

        val deletedItem = historyDao.getHistoryItemById(insertedId.toInt())
        assertNull(deletedItem)
    }

    @Test
    fun testDeleteAllHistoryItems() {
        val items = listOf(
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=test1"
                title = "Test Video 1"
                timestamp = System.currentTimeMillis()
            },
            HistoryItem().apply {
                url = "https://www.youtube.com/watch?v=test2"
                title = "Test Video 2"
                timestamp = System.currentTimeMillis()
            }
        )

        items.forEach { historyDao.insertHistoryItem(it) }

        val allItemsBefore = historyDao.getAllHistoryItems()
        assertEquals(2, allItemsBefore.size)

        historyDao.deleteAllHistoryItems()

        val allItemsAfter = historyDao.getAllHistoryItems()
        assertEquals(0, allItemsAfter.size)
    }
}