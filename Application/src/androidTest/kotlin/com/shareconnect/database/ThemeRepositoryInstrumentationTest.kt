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
class ThemeRepositoryInstrumentationTest {

    private lateinit var database: HistoryDatabase
    private lateinit var themeDao: ThemeDao
    private lateinit var themeRepository: ThemeRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()

        themeDao = database.themeDao()
        themeRepository = ThemeRepository(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveTheme() {
        val theme = Theme().apply {
            id = 1
            name = "Test Theme"
            colorScheme = "blue"
            isDarkMode = false
            isDefault = true
        }

        themeDao.insertTheme(theme)

        val retrievedTheme = themeDao.getThemeById(1)
        assertNotNull(retrievedTheme)
        assertEquals(theme.name, retrievedTheme?.name)
        assertEquals(theme.colorScheme, retrievedTheme?.colorScheme)
        assertEquals(theme.isDarkMode, retrievedTheme?.isDarkMode)
        assertEquals(theme.isDefault, retrievedTheme?.isDefault)
    }

    @Test
    fun testGetAllThemes() {
        val themes = listOf(
            Theme().apply {
                id = 1
                name = "Light Theme"
                colorScheme = "blue"
                isDarkMode = false
                isDefault = true
            },
            Theme().apply {
                id = 2
                name = "Dark Theme"
                colorScheme = "blue"
                isDarkMode = true
                isDefault = false
            }
        )

        themes.forEach { themeDao.insertTheme(it) }

        val allThemes = themeDao.getAllThemes()
        assertEquals(2, allThemes.size)
        assertTrue(allThemes.any { it.name == "Light Theme" })
        assertTrue(allThemes.any { it.name == "Dark Theme" })
    }

    @Test
    fun testGetDefaultTheme() {
        val defaultTheme = Theme().apply {
            id = 1
            name = "Default Theme"
            colorScheme = "blue"
            isDarkMode = false
            isDefault = true
        }

        val nonDefaultTheme = Theme().apply {
            id = 2
            name = "Non-Default Theme"
            colorScheme = "red"
            isDarkMode = true
            isDefault = false
        }

        themeDao.insertTheme(defaultTheme)
        themeDao.insertTheme(nonDefaultTheme)

        val retrievedDefaultTheme = themeDao.getDefaultTheme()
        assertNotNull(retrievedDefaultTheme)
        assertEquals("Default Theme", retrievedDefaultTheme?.name)
        assertTrue(retrievedDefaultTheme?.isDefault == true)
    }

    @Test
    fun testSetDefaultTheme() {
        val theme1 = Theme().apply {
            id = 1
            name = "Theme 1"
            colorScheme = "blue"
            isDarkMode = false
            isDefault = true
        }

        val theme2 = Theme().apply {
            id = 2
            name = "Theme 2"
            colorScheme = "red"
            isDarkMode = true
            isDefault = false
        }

        themeDao.insertTheme(theme1)
        themeDao.insertTheme(theme2)

        // Set theme 2 as default
        themeDao.clearAllDefaults()
        themeDao.setAsDefault(2)

        val newDefaultTheme = themeDao.getDefaultTheme()
        assertNotNull(newDefaultTheme)
        assertEquals("Theme 2", newDefaultTheme?.name)
        assertEquals(2, newDefaultTheme?.id)
    }

    @Test
    fun testUpdateTheme() {
        val theme = Theme().apply {
            id = 1
            name = "Original Theme"
            colorScheme = "blue"
            isDarkMode = false
            isDefault = false
        }

        themeDao.insertTheme(theme)

        val updatedTheme = theme.apply {
            name = "Updated Theme"
            colorScheme = "green"
            isDarkMode = true
        }

        themeDao.updateTheme(updatedTheme)

        val retrievedTheme = themeDao.getThemeById(1)
        assertNotNull(retrievedTheme)
        assertEquals("Updated Theme", retrievedTheme?.name)
        assertEquals("green", retrievedTheme?.colorScheme)
        assertTrue(retrievedTheme?.isDarkMode == true)
    }

    @Test
    fun testDeleteTheme() {
        val theme = Theme().apply {
            id = 1
            name = "Theme to Delete"
            colorScheme = "blue"
            isDarkMode = false
            isDefault = false
        }

        themeDao.insertTheme(theme)

        val insertedTheme = themeDao.getThemeById(1)
        assertNotNull(insertedTheme)

        themeDao.deleteTheme(theme)

        val deletedTheme = themeDao.getThemeById(1)
        assertNull(deletedTheme)
    }
}