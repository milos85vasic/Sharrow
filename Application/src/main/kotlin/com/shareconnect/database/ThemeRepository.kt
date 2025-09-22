package com.shareconnect.database

import android.content.Context

class ThemeRepository(context: Context) {
    private val database: HistoryDatabase
    private val themeDao: ThemeDao

    init {
        database = androidx.room.Room.databaseBuilder(
            context.applicationContext,
            HistoryDatabase::class.java, "history_database"
        )
            .allowMainThreadQueries()
            .build()
        themeDao = database.themeDao()
    }

    // Get all themes
    val allThemes: List<Theme>
        get() = themeDao.getAllThemes()

    // Get default theme
    val defaultTheme: Theme?
        get() {
            val theme = themeDao.getDefaultTheme()
            android.util.Log.d(
                "ThemeRepository", "getDefaultTheme() returned: " + if (theme != null) theme.name + " (ID: " + theme.id + ", isDefault: " + theme.isDefault + ")" else "null"
            )
            return theme
        }

    // Get theme by color scheme and mode
    fun getThemeByColorSchemeAndMode(colorScheme: String, isDarkMode: Boolean): Theme? {
        return themeDao.getThemeByColorSchemeAndMode(colorScheme, isDarkMode)
    }

    // Insert a new theme
    fun insertTheme(theme: Theme) {
        themeDao.insert(theme)
    }

    // Update a theme
    fun updateTheme(theme: Theme) {
        themeDao.update(theme)
    }

    // Set default theme
    fun setDefaultTheme(themeId: Int) {
        android.util.Log.d("ThemeRepository", "setDefaultTheme() called with themeId: $themeId")
        themeDao.clearDefaultThemes()
        themeDao.setDefaultTheme(themeId)
        android.util.Log.d("ThemeRepository", "setDefaultTheme() completed")

        // Verify the theme was set correctly
        val newDefaultTheme = defaultTheme
        if (newDefaultTheme != null) {
            android.util.Log.d(
                "ThemeRepository", "Verified new default theme: " + newDefaultTheme.name + " (ID: " + newDefaultTheme.id + ", isDefault: " + newDefaultTheme.isDefault + ")"
            )
        } else {
            android.util.Log.d("ThemeRepository", "Failed to verify new default theme - getDefaultTheme() returned null")
        }
    }

    // Initialize default themes if none exist
    fun initializeDefaultThemes() {
        android.util.Log.d("ThemeRepository", "initializeDefaultThemes() called")
        if (allThemes.isEmpty()) {
            android.util.Log.d("ThemeRepository", "No existing themes found, creating default themes")
            // Warm Orange theme
            themeDao.insert(Theme(1, "Warm Orange Light", "warm_orange", false, true))
            themeDao.insert(Theme(2, "Warm Orange Dark", "warm_orange", true, false))

            // Crimson theme
            themeDao.insert(Theme(3, "Crimson Light", "crimson", false, false))
            themeDao.insert(Theme(4, "Crimson Dark", "crimson", true, false))

            // Light Blue theme
            themeDao.insert(Theme(5, "Light Blue Light", "light_blue", false, false))
            themeDao.insert(Theme(6, "Light Blue Dark", "light_blue", true, false))

            // Purple theme
            themeDao.insert(Theme(7, "Purple Light", "purple", false, false))
            themeDao.insert(Theme(8, "Purple Dark", "purple", true, false))

            // Green theme
            themeDao.insert(Theme(9, "Green Light", "green", false, false))
            themeDao.insert(Theme(10, "Green Dark", "green", true, false))

            // Default Material theme
            themeDao.insert(Theme(11, "Material Light", "material", false, false))
            themeDao.insert(Theme(12, "Material Dark", "material", true, false))
            android.util.Log.d("ThemeRepository", "Default themes created")
        } else {
            android.util.Log.d("ThemeRepository", "Themes already exist, not creating defaults")
            val themes = allThemes
            for (theme in themes) {
                android.util.Log.d(
                    "ThemeRepository", "Existing theme: " + theme.name + " (ID: " + theme.id + ", isDefault: " + theme.isDefault + ")"
                )
            }
        }
    }
}