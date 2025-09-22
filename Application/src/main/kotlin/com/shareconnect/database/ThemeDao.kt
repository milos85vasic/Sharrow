package com.shareconnect.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes")
    fun getAllThemes(): List<Theme>

    @Query("SELECT * FROM themes WHERE isDefault = 1 LIMIT 1")
    fun getDefaultTheme(): Theme?

    @Query("SELECT * FROM themes WHERE colorScheme = :colorScheme AND isDarkMode = :isDarkMode LIMIT 1")
    fun getThemeByColorSchemeAndMode(colorScheme: String, isDarkMode: Boolean): Theme?

    @Insert
    fun insert(theme: Theme)

    @Update
    fun update(theme: Theme)

    @Query("UPDATE themes SET isDefault = 0")
    fun clearDefaultThemes()

    @Query("UPDATE themes SET isDefault = 1 WHERE id = :id")
    fun setDefaultTheme(id: Int)
}