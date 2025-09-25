package com.shareconnect

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.redelf.commons.logging.Console

class MainActivity : AppCompatActivity() {
    private var buttonSettings: MaterialButton? = null
    private var buttonHistory: MaterialButton? = null
    private var buttonOpenMeTube: MaterialButton? = null
    private var buttonManageProfiles: MaterialButton? = null
    private var buttonAddFirstProfile: MaterialButton? = null
    private var recyclerViewProfiles: RecyclerView? = null
    private var recyclerViewSystemApps: RecyclerView? = null
    private var emptyProfilesLayout: LinearLayout? = null

    private var profileManager: ProfileManager? = null
    private var themeManager: ThemeManager? = null
    private var profileAdapter: ProfileIconAdapter? = null
    private var systemAppAdapter: SystemAppAdapter? = null
    private var isContentViewSet = false

    companion object {
        private const val SETUP_WIZARD_REQUEST_CODE = 1001
        private const val EDIT_PROFILE_REQUEST_CODE = 1002
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

        setContentView(R.layout.activity_main_new)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize views
        buttonSettings = findViewById(R.id.buttonSettings)
        buttonHistory = findViewById(R.id.buttonHistory)
        buttonOpenMeTube = findViewById(R.id.buttonOpenMeTube)
        buttonManageProfiles = findViewById(R.id.buttonManageProfiles)
        buttonAddFirstProfile = findViewById(R.id.buttonAddFirstProfile)
        recyclerViewProfiles = findViewById(R.id.recyclerViewProfiles)
        recyclerViewSystemApps = findViewById(R.id.recyclerViewSystemApps)
        emptyProfilesLayout = findViewById(R.id.emptyProfilesLayout)

        val fabAdd = findViewById<ExtendedFloatingActionButton>(R.id.fabAdd)

        // Set up button listeners
        buttonSettings?.setOnClickListener {
            openSettings()
        }

        buttonHistory?.setOnClickListener {
            openHistory()
        }

        buttonOpenMeTube?.setOnClickListener {
            openDefaultService()
        }

        buttonManageProfiles?.setOnClickListener {
            openProfiles()
        }

        buttonAddFirstProfile?.setOnClickListener {
            addNewProfile()
        }

        fabAdd.setOnClickListener {
            handleAddFromClipboard()
        }

        // Set up profiles recycler view
        setupProfilesRecyclerView()

        // Set up system apps recycler view
        setupSystemAppsRecyclerView()

        // Load data
        loadProfiles()
        loadSystemApps()
    }

    private fun setupProfilesRecyclerView() {
        recyclerViewProfiles?.layoutManager = GridLayoutManager(this, 4)
    }

    private fun setupSystemAppsRecyclerView() {
        recyclerViewSystemApps?.layoutManager = GridLayoutManager(this, 4)
    }

    private fun loadProfiles() {
        val profiles = profileManager?.profiles ?: emptyList()

        if (profiles.isEmpty()) {
            recyclerViewProfiles?.visibility = View.GONE
            emptyProfilesLayout?.visibility = View.VISIBLE
        } else {
            recyclerViewProfiles?.visibility = View.VISIBLE
            emptyProfilesLayout?.visibility = View.GONE

            profileAdapter = ProfileIconAdapter(
                this,
                profiles,
                onProfileClick = { profile ->
                    openProfile(profile)
                },
                onProfileLongClick = { profile ->
                    showProfileContextMenu(profile)
                    true
                }
            )
            recyclerViewProfiles?.adapter = profileAdapter
        }

        // Update default service button text
        updateDefaultServiceButton()
    }

    private fun updateDefaultServiceButton() {
        val defaultProfile = profileManager?.defaultProfile()
        if (defaultProfile != null) {
            buttonOpenMeTube?.text = "Open ${defaultProfile.name}"
        } else {
            buttonOpenMeTube?.text = "Open Default Service"
            buttonOpenMeTube?.isEnabled = false
        }
    }

    private fun loadSystemApps() {
        // Find apps that can handle various media types
        val mediaApps = mutableListOf<ResolveInfo>()
        val pm = packageManager

        // Check for YouTube links
        val youtubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        mediaApps.addAll(pm.queryIntentActivities(youtubeIntent, 0))

        // Check for torrent files
        val torrentIntent = Intent(Intent.ACTION_VIEW)
        torrentIntent.setDataAndType(Uri.parse("file://test.torrent"), "application/x-bittorrent")
        mediaApps.addAll(pm.queryIntentActivities(torrentIntent, 0))

        // Check for magnet links
        val magnetIntent = Intent(Intent.ACTION_VIEW, Uri.parse("magnet:?xt=urn:btih:test"))
        mediaApps.addAll(pm.queryIntentActivities(magnetIntent, 0))

        // Remove duplicates and our own app
        val uniqueApps = mediaApps.distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }

        systemAppAdapter = SystemAppAdapter(
            this,
            uniqueApps.take(8), // Limit to 8 apps for better UI
            onAppClick = { app ->
                openSystemApp(app)
            }
        )
        recyclerViewSystemApps?.adapter = systemAppAdapter
    }

    private fun openProfile(profile: ServerProfile) {
        // Check if this is a torrent client or service that supports web UI with authentication
        if (profile.isTorrent() || profile.isJDownloader()) {
            // Use WebUIActivity for enhanced authentication and URL passing
            WebUIActivity.startWebUI(this, profile)
        } else {
            // For MeTube and YT-DLP, open directly in browser since they typically don't need complex auth
            val url = "${profile.url}:${profile.port}"
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open ${profile.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProfileContextMenu(profile: ServerProfile) {
        val popupMenu = PopupMenu(this, recyclerViewProfiles!!)
        popupMenu.menu.add(0, 0, 0, "Edit")
        popupMenu.menu.add(0, 1, 1, "Set as Default")
        popupMenu.menu.add(0, 2, 2, "Delete")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> editProfile(profile)
                1 -> setAsDefault(profile)
                2 -> deleteProfile(profile)
            }
            true
        }

        popupMenu.show()
    }

    private fun editProfile(profile: ServerProfile) {
        val intent = Intent(this, EditProfileActivity::class.java)
        intent.putExtra("profile_id", profile.id)
        startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
    }

    private fun setAsDefault(profile: ServerProfile) {
        profile.isDefault = true
        profileManager?.setDefaultProfile(profile)
        profileManager?.updateProfile(profile)
        loadProfiles()
        Toast.makeText(this, "${profile.name} set as default", Toast.LENGTH_SHORT).show()
    }

    private fun deleteProfile(profile: ServerProfile) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete ${profile.name}?")
            .setPositiveButton("Delete") { _, _ ->
                profileManager?.deleteProfile(profile)
                loadProfiles()
                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openSystemApp(app: ResolveInfo) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.activityInfo.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "Could not open app", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNewProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)
        startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
    }

    private fun showSetupWizard() {
        // For now, we'll just redirect to settings
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        intent.putExtra("first_run", true)
        // Use clear top to ensure clean stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        // Finish MainActivity so there's no duplicate in the stack
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SETUP_WIZARD_REQUEST_CODE -> {
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
            EDIT_PROFILE_REQUEST_CODE -> {
                // Refresh profiles after editing
                loadProfiles()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if theme has changed and recreate activity if needed
        themeManager = ThemeManager.getInstance(this)
        if (themeManager!!.hasThemeChanged()) {
            themeManager!!.resetThemeChangedFlag()
            recreate()
        }

        // Refresh data
        if (isContentViewSet) {
            loadProfiles()
            loadSystemApps()
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
                openDefaultService()
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

    private fun openProfiles() {
        val intent = Intent(this, ProfilesActivity::class.java)
        startActivity(intent)
    }

    private fun openDefaultService() {
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
        showClipboardSelectionDialog()
    }

    private fun showClipboardSelectionDialog() {
        val clipboardHistoryManager = com.shareconnect.utils.ClipboardHistoryManager.getInstance(this)
        val urlItems = clipboardHistoryManager.getUrlItems()

        if (urlItems.isEmpty()) {
            // Fallback to current clipboard behavior
            val currentText = clipboardHistoryManager.getCurrentClipboardText()
            if (currentText != null && isValidUrl(currentText)) {
                processClipboardUrl(currentText)
            } else {
                Toast.makeText(this, "No valid URLs found in clipboard history", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Show dialog with clipboard history
        val dialogView = layoutInflater.inflate(R.layout.dialog_clipboard_selection, null)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewClipboardItems)
        val emptyState = dialogView.findViewById<LinearLayout>(R.id.emptyClipboardState)
        val buttonClearHistory = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonClearHistory)
        val buttonCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCancel)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        if (urlItems.isNotEmpty()) {
            recyclerView.visibility = android.view.View.VISIBLE
            emptyState.visibility = android.view.View.GONE

            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            recyclerView.adapter = ClipboardAdapter(urlItems) { clipboardItem ->
                dialog.dismiss()
                processClipboardUrl(clipboardItem.text)
            }
        } else {
            recyclerView.visibility = android.view.View.GONE
            emptyState.visibility = android.view.View.VISIBLE
        }

        buttonClearHistory.setOnClickListener {
            clipboardHistoryManager.clearHistory()
            dialog.dismiss()
            Toast.makeText(this, "Clipboard history cleared", Toast.LENGTH_SHORT).show()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun processClipboardUrl(url: String) {
        if (isValidUrl(url)) {
            val intent = Intent(this, ShareActivity::class.java)
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, url)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Invalid URL: $url", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Simple URL validation
     */
    private fun isValidUrl(url: String?): Boolean {
        if (url == null) return false
        return try {
            val uri = Uri.parse(url)
            uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https" ||
                uri.scheme == "magnet" || url.endsWith(".torrent"))
        } catch (e: Exception) {
            false
        }
    }
}