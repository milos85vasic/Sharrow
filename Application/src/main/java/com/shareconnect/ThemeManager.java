package com.shareconnect;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.shareconnect.database.Theme;
import com.shareconnect.database.ThemeRepository;

public class ThemeManager {
    private static ThemeManager instance;
    private ThemeRepository themeRepository;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_CHANGED = "theme_changed";
    
    private ThemeManager(Context context) {
        themeRepository = new ThemeRepository(context);
        themeRepository.initializeDefaultThemes();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void applyTheme(Activity activity) {
        Theme defaultTheme = themeRepository.getDefaultTheme();
        if (defaultTheme != null) {
            applyTheme(activity, defaultTheme);
        } else {
            // Apply default material theme
            activity.setTheme(R.style.Theme_ShareConnect_Material_Light);
        }
    }
    
    public void applyTheme(Activity activity, Theme theme) {
        String colorScheme = theme.getColorScheme();
        boolean isDarkMode = theme.isDarkMode();
        
        String currentTheme = colorScheme + (isDarkMode ? "_DARK" : "_LIGHT");
        
        // Check if activity uses Toolbar (requires NoActionBar theme)
        boolean usesToolbar = activity instanceof ThemeSelectionActivity || activity instanceof SettingsActivity || activity instanceof SplashActivity || activity instanceof ProfilesActivity;
        
        switch (currentTheme) {
            case "WARM_ORANGE_DARK":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_WarmOrange_Dark_NoActionBar : 
                    R.style.Theme_ShareConnect_WarmOrange_Dark);
                break;
            case "WARM_ORANGE_LIGHT":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_WarmOrange_Light_NoActionBar : 
                    R.style.Theme_ShareConnect_WarmOrange_Light);
                break;
            case "CRIMSON_DARK":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Crimson_Dark_NoActionBar : 
                    R.style.Theme_ShareConnect_Crimson_Dark);
                break;
            case "CRIMSON_LIGHT":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Crimson_Light_NoActionBar : 
                    R.style.Theme_ShareConnect_Crimson_Light);
                break;
            case "LIGHT_BLUE_DARK":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_LightBlue_Dark_NoActionBar : 
                    R.style.Theme_ShareConnect_LightBlue_Dark);
                break;
            case "LIGHT_BLUE_LIGHT":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_LightBlue_Light_NoActionBar : 
                    R.style.Theme_ShareConnect_LightBlue_Light);
                break;
            case "PURPLE_DARK":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Purple_Dark_NoActionBar : 
                    R.style.Theme_ShareConnect_Purple_Dark);
                break;
            case "PURPLE_LIGHT":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Purple_Light_NoActionBar : 
                    R.style.Theme_ShareConnect_Purple_Light);
                break;
            case "GREEN_DARK":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Green_Dark_NoActionBar : 
                    R.style.Theme_ShareConnect_Green_Dark);
                break;
            case "GREEN_LIGHT":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Green_Light_NoActionBar : 
                    R.style.Theme_ShareConnect_Green_Light);
                break;
            case "MATERIAL_DARK":
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Material_Dark_NoActionBar : 
                    R.style.Theme_ShareConnect_Material_Dark);
                break;
            case "MATERIAL_LIGHT":
            default:
                activity.setTheme(usesToolbar ? 
                    R.style.Theme_ShareConnect_Material_Light_NoActionBar : 
                    R.style.Theme_ShareConnect_Material_Light);
                break;
        }
        
        // Apply day/night mode
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    public ThemeRepository getThemeRepository() {
        return themeRepository;
    }
    
    // Method to notify that theme has changed
    public void notifyThemeChanged() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_THEME_CHANGED, true);
        editor.commit();
    }
    
    // Method to reset theme changed flag
    public void resetThemeChangedFlag() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_THEME_CHANGED, false);
        editor.commit();
    }
    
    // Method to check if theme has changed
    public boolean hasThemeChanged() {
        return sharedPreferences.getBoolean(KEY_THEME_CHANGED, false);
    }
}