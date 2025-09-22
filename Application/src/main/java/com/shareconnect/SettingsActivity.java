package com.shareconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    private static final int THEME_SELECTION_REQUEST = 1001;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply current theme before calling super.onCreate()
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if theme has changed and recreate activity if needed
        if (themeManager != null && themeManager.hasThemeChanged()) {
            themeManager.resetThemeChangedFlag();
            recreate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If theme was changed, recreate this activity to apply the new theme
        if (requestCode == THEME_SELECTION_REQUEST && resultCode == RESULT_OK) {
            recreate();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            
            Preference profilesPreference = findPreference("server_profiles");
            if (profilesPreference != null) {
                profilesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getContext(), ProfilesActivity.class);
                        startActivity(intent);
                        return true;
                    }
                });
            }
            
            Preference themePreference = findPreference("theme_selection");
            if (themePreference != null) {
                themePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getContext(), ThemeSelectionActivity.class);
                        // Start for result to know when theme changes
                        startActivityForResult(intent, THEME_SELECTION_REQUEST);
                        return true;
                    }
                });
            }
        }
    }
}