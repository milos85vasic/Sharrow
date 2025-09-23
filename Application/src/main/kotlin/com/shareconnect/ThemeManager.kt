package com.shareconnect

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.shareconnect.database.Theme
import com.shareconnect.database.ThemeRepository

class ThemeManager private constructor(context: Context) {
    private val themeRepository: ThemeRepository
    private val sharedPreferences: SharedPreferences

    init {
        themeRepository = ThemeRepository(context)
        themeRepository.initializeDefaultThemes()
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun applyTheme(activity: Activity) {
        val defaultTheme = themeRepository.defaultTheme
        android.util.Log.d("ThemeManager", "getDefaultTheme() returned: " + if (defaultTheme != null) defaultTheme.name + " (ID: " + defaultTheme.id + ", isDefault: " + defaultTheme.isDefault + ")" else "null")
        if (defaultTheme != null) {
            applyTheme(activity, defaultTheme)
        } else {
            // Apply default material theme
            android.util.Log.d("ThemeManager", "Applying default material theme")
            activity.setTheme(R.style.Theme_ShareConnect_Material_Light)
        }
    }

    fun applyTheme(activity: Activity, theme: Theme) {
        val colorScheme = theme.colorScheme
        val isDarkMode = theme.isDarkMode

        val currentTheme = (colorScheme ?: "").uppercase() + if (isDarkMode) "_DARK" else "_LIGHT"

        android.util.Log.d("ThemeManager", "Applying theme: " + theme.name + ", colorScheme: " + colorScheme + ", isDarkMode: " + isDarkMode + ", currentTheme: " + currentTheme)

        // Check if activity uses Toolbar (requires NoActionBar theme)
        val usesToolbar = activity is MainActivity || activity is ThemeSelectionActivity || activity is SettingsActivity || activity is SplashActivity || activity is ProfilesActivity || activity is ShareActivity || activity is EditProfileActivity || activity is HistoryActivity

        android.util.Log.d("ThemeManager", "Activity " + activity.javaClass.simpleName + " usesToolbar: " + usesToolbar)

        when (currentTheme) {
            "WARM_ORANGE_DARK" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_WarmOrange_Dark_NoActionBar
                else
                    R.style.Theme_ShareConnect_WarmOrange_Dark
            )
            "WARM_ORANGE_LIGHT" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_WarmOrange_Light_NoActionBar
                else
                    R.style.Theme_ShareConnect_WarmOrange_Light
            )
            "CRIMSON_DARK" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Crimson_Dark_NoActionBar
                else
                    R.style.Theme_ShareConnect_Crimson_Dark
            )
            "CRIMSON_LIGHT" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Crimson_Light_NoActionBar
                else
                    R.style.Theme_ShareConnect_Crimson_Light
            )
            "LIGHT_BLUE_DARK" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_LightBlue_Dark_NoActionBar
                else
                    R.style.Theme_ShareConnect_LightBlue_Dark
            )
            "LIGHT_BLUE_LIGHT" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_LightBlue_Light_NoActionBar
                else
                    R.style.Theme_ShareConnect_LightBlue_Light
            )
            "PURPLE_DARK" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Purple_Dark_NoActionBar
                else
                    R.style.Theme_ShareConnect_Purple_Dark
            )
            "PURPLE_LIGHT" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Purple_Light_NoActionBar
                else
                    R.style.Theme_ShareConnect_Purple_Light
            )
            "GREEN_DARK" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Green_Dark_NoActionBar
                else
                    R.style.Theme_ShareConnect_Green_Dark
            )
            "GREEN_LIGHT" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Green_Light_NoActionBar
                else
                    R.style.Theme_ShareConnect_Green_Light
            )
            "MATERIAL_DARK" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Material_Dark_NoActionBar
                else
                    R.style.Theme_ShareConnect_Material_Dark
            )
            "MATERIAL_LIGHT" -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Material_Light_NoActionBar
                else
                    R.style.Theme_ShareConnect_Material_Light
            )
            else -> activity.setTheme(
                if (usesToolbar)
                    R.style.Theme_ShareConnect_Material_Light_NoActionBar
                else
                    R.style.Theme_ShareConnect_Material_Light
            )
        }

        // Apply day/night mode
        if (isDarkMode) {
            android.util.Log.d("ThemeManager", "Setting night mode to YES")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            android.util.Log.d("ThemeManager", "Setting night mode to NO")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    val themeRepositoryVal: ThemeRepository
        get() = themeRepository

    // Method to notify that theme has changed
    fun notifyThemeChanged() {
        android.util.Log.d("ThemeManager", "notifyThemeChanged() called")
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_THEME_CHANGED, true)
        editor.commit()
        android.util.Log.d("ThemeManager", "notifyThemeChanged() completed, SharedPreferences updated")
    }

    // Method to reset theme changed flag
    fun resetThemeChangedFlag() {
        android.util.Log.d("ThemeManager", "resetThemeChangedFlag() called")
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_THEME_CHANGED, false)
        editor.commit()
        android.util.Log.d("ThemeManager", "resetThemeChangedFlag() completed, SharedPreferences updated")
    }

    // Method to check if theme has changed
    fun hasThemeChanged(): Boolean {
        val changed = sharedPreferences.getBoolean(KEY_THEME_CHANGED, false)
        android.util.Log.d("ThemeManager", "hasThemeChanged() returned: $changed")
        return changed
    }

    companion object {
        private var instance: ThemeManager? = null
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_CHANGED = "theme_changed"

        @Synchronized
        fun getInstance(context: Context): ThemeManager {
            if (instance == null) {
                instance = ThemeManager(context.applicationContext)
            }
            return instance!!
        }
    }
}