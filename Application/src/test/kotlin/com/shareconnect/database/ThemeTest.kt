package com.shareconnect.database

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class ThemeTest {

    private lateinit var theme: Theme

    @Before
    fun setUp() {
        theme = Theme()
    }

    @Test
    fun testThemeInitialization() {
        assertNotNull(theme)
        assertEquals(0, theme.id)
        assertNull(theme.name)
        assertNull(theme.colorScheme)
        assertFalse(theme.isDarkMode)
        assertFalse(theme.isDefault)
    }

    @Test
    fun testThemeSettersAndGetters() {
        val id = 1
        val name = "Test Theme"
        val colorScheme = "blue"
        val isDarkMode = true
        val isDefault = true

        theme.id = id
        theme.name = name
        theme.colorScheme = colorScheme
        theme.isDarkMode = isDarkMode
        theme.isDefault = isDefault

        assertEquals(id, theme.id)
        assertEquals(name, theme.name)
        assertEquals(colorScheme, theme.colorScheme)
        assertEquals(isDarkMode, theme.isDarkMode)
        assertEquals(isDefault, theme.isDefault)
    }

    @Test
    fun testThemeEquality() {
        val theme1 = Theme().apply {
            id = 1
            name = "Test Theme"
            colorScheme = "blue"
            isDarkMode = true
            isDefault = false
        }

        val theme2 = Theme().apply {
            id = 1
            name = "Test Theme"
            colorScheme = "blue"
            isDarkMode = true
            isDefault = false
        }

        assertEquals(theme1.id, theme2.id)
        assertEquals(theme1.name, theme2.name)
        assertEquals(theme1.colorScheme, theme2.colorScheme)
        assertEquals(theme1.isDarkMode, theme2.isDarkMode)
        assertEquals(theme1.isDefault, theme2.isDefault)
    }

    @Test
    fun testThemeWithDifferentValues() {
        val lightTheme = Theme().apply {
            id = 1
            name = "Light Theme"
            colorScheme = "blue"
            isDarkMode = false
            isDefault = true
        }

        val darkTheme = Theme().apply {
            id = 2
            name = "Dark Theme"
            colorScheme = "red"
            isDarkMode = true
            isDefault = false
        }

        assertNotEquals(lightTheme.id, darkTheme.id)
        assertNotEquals(lightTheme.name, darkTheme.name)
        assertNotEquals(lightTheme.colorScheme, darkTheme.colorScheme)
        assertNotEquals(lightTheme.isDarkMode, darkTheme.isDarkMode)
        assertNotEquals(lightTheme.isDefault, darkTheme.isDefault)
    }

    @Test
    fun testThemeDefaultValues() {
        val defaultTheme = Theme()

        assertEquals(0, defaultTheme.id)
        assertNull(defaultTheme.name)
        assertNull(defaultTheme.colorScheme)
        assertFalse(defaultTheme.isDarkMode)
        assertFalse(defaultTheme.isDefault)
    }
}