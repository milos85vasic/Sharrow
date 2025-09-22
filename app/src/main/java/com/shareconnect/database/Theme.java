package com.shareconnect.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "themes")
public class Theme {
    @PrimaryKey
    private int id;
    
    private String name;
    private String colorScheme;
    private boolean isDarkMode;
    private boolean isDefault;
    
    // Constructors
    public Theme() {}
    
    public Theme(int id, String name, String colorScheme, boolean isDarkMode, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.colorScheme = colorScheme;
        this.isDarkMode = isDarkMode;
        this.isDefault = isDefault;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getColorScheme() {
        return colorScheme;
    }
    
    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }
    
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}