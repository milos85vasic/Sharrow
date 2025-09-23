package com.shareconnect

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private var buttonSettings: MaterialButton? = null
    private var buttonOpenMeTube: MaterialButton? = null
    private var profileManager: ProfileManager? = null
    private var themeManager: ThemeManager? = null
    private var isContentViewSet = false

    companion object {
        private const val SETUP_WIZARD_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content and calling super.onCreate()
        themeManager = ThemeManager.getInstance(this)
        themeManager!!.applyTheme(this)

        super.onCreate(savedInstanceState)

        profileManager = ProfileManager(this)

        // Check if we have any profiles configured
        if (!profileManager!!.hasProfiles()) {
            // Show setup wizard
            showSetupWizard()
            // Don't return here - let the activity finish its onCreate
            // The activity will be finished after settings if still no profiles
        } else {
            // Only set content view if we have profiles
            setupMainView()
        }
    }

    private fun setupMainView() {
        // Only set up the view once
        if (isContentViewSet) {
            return
        }
        isContentViewSet = true

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        buttonSettings = findViewById(R.id.buttonSettings)
        buttonOpenMeTube = findViewById(R.id.buttonOpenMeTube)
        val buttonHistory = findViewById<MaterialButton>(R.id.buttonHistory)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        buttonSettings!!.setOnClickListener {
            openSettings()
        }

        buttonOpenMeTube!!.setOnClickListener {
            openMeTubeInterface()
        }

        buttonHistory.setOnClickListener {
            openHistory()
        }

        fabAdd.setOnClickListener {
            handleAddFromClipboard()
        }
    }

    private fun showSetupWizard() {
        // For now, we'll just redirect to settings
        // In a more complete implementation, we could show a guided setup wizard
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        intent.putExtra("first_run", true)
        startActivityForResult(intent, SETUP_WIZARD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SETUP_WIZARD_REQUEST_CODE) {
            // Check if profiles were created
            if (!profileManager!!.hasProfiles()) {
                // Still no profiles, finish the activity
                Toast.makeText(this, R.string.please_configure_profile_custom, Toast.LENGTH_LONG).show()
                finish()
            } else {
                // Profiles created, set up the main view
                setupMainView()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.action_settings -> {
                openSettings()
                true
            }
            R.id.action_open_metube -> {
                openMeTubeInterface()
                true
            }
            R.id.action_history -> {
                openHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun openMeTubeInterface() {
        val defaultProfile = profileManager!!.defaultProfile()

        if (defaultProfile == null) {
            Toast.makeText(this, "Please set a default profile in Settings", Toast.LENGTH_SHORT).show()
            return
        }

        val url = defaultProfile.url + ":" + defaultProfile.port
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    /**
     * Handle adding a URL from clipboard
     */
    private fun handleAddFromClipboard() {
        // Get clipboard manager
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Check if clipboard has primary clip
        if (clipboard.hasPrimaryClip()) {
            val clipData = clipboard.primaryClip

            // Check if clip data is not null and has at least one item
            if (clipData != null && clipData.itemCount > 0) {
                // Get the text from the first item
                val clipboardText = clipData.getItemAt(0).text

                if (clipboardText != null) {
                    val url = clipboardText.toString().trim { it <= ' ' }

                    // Validate URL
                    if (isValidUrl(url)) {
                        // Open ShareActivity with the URL from clipboard
                        val intent = Intent(this, ShareActivity::class.java)
                        intent.action = Intent.ACTION_SEND
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_TEXT, url)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Invalid URL in clipboard", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No text found in clipboard", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Simple URL validation
     */
    private fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrEmpty()) {
            return false
        }

        // Check if it starts with http:// or https://
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false
        }

        // Basic validation - check if it contains a domain
        return try {
            val uri = Uri.parse(url)
            uri.host != null && uri.host!!.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if theme has changed and recreate activity if needed
        themeManager = ThemeManager.getInstance(this)
        val themeChanged = themeManager!!.hasThemeChanged()
        android.util.Log.d("MainActivity", "onResume() called, themeChanged: $themeChanged")
        if (themeChanged) {
            themeManager!!.resetThemeChangedFlag()
            android.util.Log.d("MainActivity", "Recreating activity due to theme change")
            recreate()
        }
    }
}