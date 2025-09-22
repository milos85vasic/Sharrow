package com.metubeshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ThemeDao {
    @Query("SELECT * FROM themes")
    List<Theme> getAllThemes();
    
    @Query("SELECT * FROM themes WHERE isDefault = 1 LIMIT 1")
    Theme getDefaultTheme();
    
    @Query("SELECT * FROM themes WHERE colorScheme = :colorScheme AND isDarkMode = :isDarkMode LIMIT 1")
    Theme getThemeByColorSchemeAndMode(String colorScheme, boolean isDarkMode);
    
    @Insert
    void insert(Theme theme);
    
    @Update
    void update(Theme theme);
    
    @Query("UPDATE themes SET isDefault = 0")
    void clearDefaultThemes();
    
    @Query("UPDATE themes SET isDefault = 1 WHERE id = :id")
    void setDefaultTheme(int id);
}