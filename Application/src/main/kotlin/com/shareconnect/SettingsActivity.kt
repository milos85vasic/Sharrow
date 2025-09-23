package com.shareconnect

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    private var themeManager: ThemeManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply current theme before calling super.onCreate()
        themeManager = ThemeManager.getInstance(this)
        themeManager!!.applyTheme(this)

        super.onCreate(savedInstanceState)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // If theme was changed, restart this activity to apply the new theme
        if (requestCode == THEME_SELECTION_REQUEST && resultCode == RESULT_OK) {
            android.util.Log.d("SettingsActivity", "Theme change detected, restarting activity")
            // Create a new intent for this activity
            val intent = Intent(this, SettingsActivity::class.java)
            // Clear the current activity from the stack and start fresh
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            // Finish the current instance
            finish()
            // Override the transition to make it seamless
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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