package com.shareconnect;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import com.shareconnect.database.Theme;
import com.shareconnect.database.ThemeRepository;

public class ThemeManager {
    private static ThemeManager instance;
    private ThemeRepository themeRepository;
    
    private ThemeManager(Context context) {
        themeRepository = new ThemeRepository(context);
        themeRepository.initializeDefaultThemes();
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context);
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
        
        switch (currentTheme) {
            case "WARM_ORANGE_DARK":
                activity.setTheme(R.style.Theme_ShareConnect_WarmOrange_Dark);
                break;
            case "WARM_ORANGE_LIGHT":
                activity.setTheme(R.style.Theme_ShareConnect_WarmOrange_Light);
                break;
            case "CRIMSON_DARK":
                activity.setTheme(R.style.Theme_ShareConnect_Crimson_Dark);
                break;
            case "CRIMSON_LIGHT":
                activity.setTheme(R.style.Theme_ShareConnect_Crimson_Light);
                break;
            case "LIGHT_BLUE_DARK":
                activity.setTheme(R.style.Theme_ShareConnect_LightBlue_Dark);
                break;
            case "LIGHT_BLUE_LIGHT":
                activity.setTheme(R.style.Theme_ShareConnect_LightBlue_Light);
                break;
            case "PURPLE_DARK":
                activity.setTheme(R.style.Theme_ShareConnect_Purple_Dark);
                break;
            case "PURPLE_LIGHT":
                activity.setTheme(R.style.Theme_ShareConnect_Purple_Light);
                break;
            case "GREEN_DARK":
                activity.setTheme(R.style.Theme_ShareConnect_Green_Dark);
                break;
            case "GREEN_LIGHT":
                activity.setTheme(R.style.Theme_ShareConnect_Green_Light);
                break;
            case "MATERIAL_DARK":
                activity.setTheme(R.style.Theme_ShareConnect_Material_Dark);
                break;
            case "MATERIAL_LIGHT":
            default:
                activity.setTheme(R.style.Theme_ShareConnect_Material_Light);
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
}