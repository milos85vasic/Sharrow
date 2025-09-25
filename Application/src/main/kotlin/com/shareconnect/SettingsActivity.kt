package com.shareconnect

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.redelf.commons.logging.Console

class SettingsActivity : AppCompatActivity() {
    private var themeManager: ThemeManager? = null
    private var isFirstRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply current theme before calling super.onCreate()
        themeManager = ThemeManager.getInstance(this)
        themeManager!!.applyTheme(this)

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Check if this is the first run
        isFirstRun = intent.getBooleanExtra("first_run", false)

        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isFirstRun) {
            // If this is the first run, we need to check if profiles were created
            val profileManager = ProfileManager(this)
            if (profileManager.hasProfiles()) {
                // Profiles were created, start MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            } else {
                // No profiles created, just finish and let the app close
                finishAffinity() // This will finish all activities and properly close the app
            }
        } else {
            // Normal back behavior
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // If theme was changed, recreate this activity to apply the new theme
        if (requestCode == THEME_SELECTION_REQUEST && resultCode == RESULT_OK) {
            Console.debug("Theme change detected, recreating activity")
            // Simply recreate the current activity instead of starting a new one
            recreate()
        }
    }

    fun startThemeSelection() {
        val intent = Intent(this, ThemeSelectionActivity::class.java)
        startActivityForResult(intent, THEME_SELECTION_REQUEST)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val profilesPreference = findPreference<Preference>("server_profiles")
            if (profilesPreference != null) {
                profilesPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    val intent = Intent(context, ProfilesActivity::class.java)
                    startActivity(intent)
                    true
                }
            }

            val themePreference = findPreference<Preference>("theme_selection")
            if (themePreference != null) {
                themePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    // Call the parent activity's method to start theme selection
                    (activity as? SettingsActivity)?.startThemeSelection()
                    true
                }
            }
        }
    }

    companion object {
        private const val THEME_SELECTION_REQUEST = 1001
    }
}