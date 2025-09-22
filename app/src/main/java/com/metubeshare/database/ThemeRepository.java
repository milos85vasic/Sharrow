package com.metubeshare.database;

import android.content.Context;
import java.util.List;

public class ThemeRepository {
    private HistoryDatabase database;
    private ThemeDao themeDao;
    
    public ThemeRepository(Context context) {
        database = androidx.room.Room.databaseBuilder(context.getApplicationContext(),
                HistoryDatabase.class, "history_database")
                .allowMainThreadQueries()
                .build();
        themeDao = database.themeDao();
    }
    
    // Get all themes
    public List<Theme> getAllThemes() {
        return themeDao.getAllThemes();
    }
    
    // Get default theme
    public Theme getDefaultTheme() {
        return themeDao.getDefaultTheme();
    }
    
    // Get theme by color scheme and mode
    public Theme getThemeByColorSchemeAndMode(String colorScheme, boolean isDarkMode) {
        return themeDao.getThemeByColorSchemeAndMode(colorScheme, isDarkMode);
    }
    
    // Insert a new theme
    public void insertTheme(Theme theme) {
        themeDao.insert(theme);
    }
    
    // Update a theme
    public void updateTheme(Theme theme) {
        themeDao.update(theme);
    }
    
    // Set default theme
    public void setDefaultTheme(int themeId) {
        themeDao.clearDefaultThemes();
        themeDao.setDefaultTheme(themeId);
    }
    
    // Initialize default themes if none exist
    public void initializeDefaultThemes() {
        if (getAllThemes().isEmpty()) {
            // Warm Orange theme
            themeDao.insert(new Theme(1, "Warm Orange Light", "warm_orange", false, true));
            themeDao.insert(new Theme(2, "Warm Orange Dark", "warm_orange", true, false));
            
            // Crimson theme
            themeDao.insert(new Theme(3, "Crimson Light", "crimson", false, false));
            themeDao.insert(new Theme(4, "Crimson Dark", "crimson", true, false));
            
            // Light Blue theme
            themeDao.insert(new Theme(5, "Light Blue Light", "light_blue", false, false));
            themeDao.insert(new Theme(6, "Light Blue Dark", "light_blue", true, false));
            
            // Purple theme
            themeDao.insert(new Theme(7, "Purple Light", "purple", false, false));
            themeDao.insert(new Theme(8, "Purple Dark", "purple", true, false));
            
            // Green theme
            themeDao.insert(new Theme(9, "Green Light", "green", false, false));
            themeDao.insert(new Theme(10, "Green Dark", "green", true, false));
            
            // Default Material theme
            themeDao.insert(new Theme(11, "Material Light", "material", false, false));
            themeDao.insert(new Theme(12, "Material Dark", "material", true, false));
        }
    }
}