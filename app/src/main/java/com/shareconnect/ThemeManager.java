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
            activity.setTheme(R.style.Theme_MeTubeShare_Material_Light);
        }
    }
    
    public void applyTheme(Activity activity, Theme theme) {
        String colorScheme = theme.getColorScheme();
        boolean isDarkMode = theme.isDarkMode();
        
        switch (colorScheme) {
            case "warm_orange":
                if (isDarkMode) {
                    activity.setTheme(R.style.Theme_MeTubeShare_WarmOrange_Dark);
                } else {
                    activity.setTheme(R.style.Theme_MeTubeShare_WarmOrange_Light);
                }
                break;
            case "crimson":
                if (isDarkMode) {
                    activity.setTheme(R.style.Theme_MeTubeShare_Crimson_Dark);
                } else {
                    activity.setTheme(R.style.Theme_MeTubeShare_Crimson_Light);
                }
                break;
            case "light_blue":
                if (isDarkMode) {
                    activity.setTheme(R.style.Theme_MeTubeShare_LightBlue_Dark);
                } else {
                    activity.setTheme(R.style.Theme_MeTubeShare_LightBlue_Light);
                }
                break;
            case "purple":
                if (isDarkMode) {
                    activity.setTheme(R.style.Theme_MeTubeShare_Purple_Dark);
                } else {
                    activity.setTheme(R.style.Theme_MeTubeShare_Purple_Light);
                }
                break;
            case "green":
                if (isDarkMode) {
                    activity.setTheme(R.style.Theme_MeTubeShare_Green_Dark);
                } else {
                    activity.setTheme(R.style.Theme_MeTubeShare_Green_Light);
                }
                break;
            case "material":
            default:
                if (isDarkMode) {
                    activity.setTheme(R.style.Theme_MeTubeShare_Material_Dark);
                } else {
                    activity.setTheme(R.style.Theme_MeTubeShare_Material_Light);
                }
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