package com.shareconnect.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes")
    fun getAllThemes(): List<Theme>

    @Query("SELECT * FROM themes WHERE id = :id")
    fun getThemeById(id: Int): Theme?

    @Query("SELECT * FROM themes WHERE isDefault = 1 LIMIT 1")
    fun getDefaultTheme(): Theme?

    @Query("SELECT * FROM themes WHERE colorScheme = :colorScheme AND isDarkMode = :isDarkMode LIMIT 1")
    fun getThemeByColorSchemeAndMode(colorScheme: String, isDarkMode: Boolean): Theme?

    @Insert
    fun insert(theme: Theme): Long

    @Insert
    fun insertTheme(theme: Theme): Long

    @Update
    fun update(theme: Theme)

    @Update
    fun updateTheme(theme: Theme)

    @Delete
    fun delete(theme: Theme)

    @Delete
    fun deleteTheme(theme: Theme)

    @Query("UPDATE themes SET isDefault = 0")
    fun clearDefaultThemes()

    @Query("UPDATE themes SET isDefault = 0")
    fun clearAllDefaults()

    @Query("UPDATE themes SET isDefault = 1 WHERE id = :id")
    fun setDefaultTheme(id: Int)

    @Query("UPDATE themes SET isDefault = 1 WHERE id = :id")
    fun setAsDefault(id: Int)
}