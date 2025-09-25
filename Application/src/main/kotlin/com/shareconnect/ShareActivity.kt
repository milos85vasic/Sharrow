package com.shareconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.shareconnect.database.HistoryItem
import com.shareconnect.database.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareActivity : AppCompatActivity() {
    private var textViewMediaLink: TextView? = null
    private var autoCompleteProfiles: AutoCompleteTextView? = null
    private var buttonSendToService: MaterialButton? = null
    private var buttonShareToApps: MaterialButton? = null
    private var progressBar: ProgressBar? = null
    private var profiles: List<ServerProfile> = ArrayList()
    private var mediaLink: String? = null
    private var serviceApiClient: ServiceApiClient? = null
    private var profileManager: ProfileManager? = null
    private var themeManager: ThemeManager? = null
    private var metadataFetcher: MetadataFetcher? = null
    private var urlMetadata: UrlMetadata? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this)
        themeManager!!.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        profileManager = ProfileManager(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initViews()
        handleIntent()
        loadProfiles()
        setupListeners()
        serviceApiClient = ServiceApiClient()
        metadataFetcher = MetadataFetcher()
        fetchMetadataForUrl()
    }

    override fun onResume() {
        super.onResume()
        // Check if theme has changed and recreate activity if needed
        themeManager = ThemeManager.getInstance(this)
        if (themeManager!!.hasThemeChanged()) {
            themeManager!!.resetThemeChangedFlag()
            recreate()
        }
    }

    private fun initViews() {
        textViewMediaLink = findViewById(R.id.textViewYouTubeLink)
        autoCompleteProfiles = findViewById(R.id.autoCompleteProfiles)
        buttonSendToService = findViewById(R.id.buttonSendToMeTube)
        buttonShareToApps = findViewById(R.id.buttonShareToApps)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun handleIntent() {
        val intent = intent
        val action = intent.action
        val type = intent.type
        val data = intent.data

        if (Intent.ACTION_SEND == action && "text/plain" == type) {
            // Handle shared text (URL)
            mediaLink = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (mediaLink != null) {
                textViewMediaLink!!.text = mediaLink
            }
        } else if (Intent.ACTION_VIEW == action && data != null) {
            // Handle direct URL intent
            mediaLink = data.toString()
            textViewMediaLink!!.text = mediaLink
        } else {
            // No valid link received
            mediaLink = null
            textViewMediaLink!!.setText(R.string.no_media_link_received)
        }
    }

    private fun fetchMetadataForUrl() {
        if (mediaLink.isNullOrEmpty()) return

        // Show progress while fetching metadata
        progressBar?.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                urlMetadata = metadataFetcher?.fetchMetadata(mediaLink!!)

                // Update UI with fetched metadata
                urlMetadata?.let { metadata ->
                    // Update the display with title if available
                    if (!metadata.title.isNullOrEmpty()) {
                        textViewMediaLink?.text = buildString {
                            append(metadata.title)
                            if (!metadata.siteName.isNullOrEmpty()) {
                                append(" - ")
                                append(metadata.siteName)
                            }
                            append("\n")
                            append(mediaLink)
                        }
                    }
                }
            } catch (e: Exception) {
                // Failed to fetch metadata, use simple extraction
                e.printStackTrace()
            } finally {
                progressBar?.visibility = View.GONE
            }
        }
    }

    private fun loadProfiles() {
        profiles = profileManager!!.profiles

        if (profiles.isEmpty()) {
            // Show a message and redirect to settings
            Toast.makeText(this, R.string.please_configure_profile_custom, Toast.LENGTH_LONG).show()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Create adapter for AutoCompleteTextView
        val profileNames: MutableList<String> = ArrayList()
        for (profile in profiles) {
            profileNames.add(profile.name + " (" + profile.getServiceTypeName(this) + ")")
        }

        val adapter = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line, profileNames
        )
        autoCompleteProfiles!!.setAdapter(adapter)

        // Set default selection if there's a default profile
        val defaultProfile = profileManager!!.defaultProfile()
        if (defaultProfile != null) {
            autoCompleteProfiles!!.setText(
                defaultProfile.name + " (" + defaultProfile.getServiceTypeName(this) + ")", false
            )
        } else if (profileNames.isNotEmpty()) {
            // If no default, select the first one
            autoCompleteProfiles!!.setText(profileNames[0], false)
        }
    }

    private fun setupListeners() {
        buttonSendToService!!.setOnClickListener {
            sendToService()
        }

        buttonShareToApps!!.setOnClickListener {
            shareToApps()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.share_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_open_metube -> {
                openServiceInterface()
                true
            }
            R.id.action_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendToService() {
        if (mediaLink.isNullOrEmpty()) {
            Toast.makeText(this, R.string.no_youtube_link, Toast.LENGTH_SHORT).show()
            return
        }

        if (profiles.isEmpty()) {
            Toast.makeText(this, R.string.please_configure_profile_custom, Toast.LENGTH_SHORT).show()
            return
        }

        val selectedProfileText = autoCompleteProfiles!!.text.toString()
        var selectedProfile: ServerProfile? = null

        // Extract the profile name from the displayed text (name + service type)
        var profileName = selectedProfileText
        val parenIndex = selectedProfileText.indexOf(" (")
        if (parenIndex > 0) {
            profileName = selectedProfileText.substring(0, parenIndex)
        }

        for (profile in profiles) {
            if (profile.name == profileName) {
                selectedProfile = profile
                break
            }
        }

        if (selectedProfile == null) {
            Toast.makeText(this, R.string.please_configure_profile_custom, Toast.LENGTH_SHORT).show()
            return
        }

        // Store the profile URL and port for use in the callback
        val profileUrl = selectedProfile.url
        val profilePort = selectedProfile.port
        val profileId = selectedProfile.id
        val profileNameFinal = selectedProfile.name
        val serviceTypeName = selectedProfile.getServiceTypeName(this)

        // Check if this is a torrent client or jDownloader that should use WebUI
        if (selectedProfile.isTorrent() || selectedProfile.isJDownloader()) {
            // Use WebUIActivity for torrent clients and jDownloader with automatic authentication and URL passing
            WebUIActivity.startWebUI(this, selectedProfile, mediaLink!!)

            // Save to history immediately as successful (WebUI will handle the actual sending)
            saveToHistory(mediaLink!!, profileId!!, profileNameFinal!!, serviceTypeName, true)
            return
        }

        // For MeTube and YT-DLP, use the existing API approach
        // Show progress
        progressBar!!.visibility = View.VISIBLE
        buttonSendToService!!.isEnabled = false
        buttonSendToService!!.text = "Sending to $serviceTypeName..."

        // Call the service API
        serviceApiClient!!.sendUrlToService(
            selectedProfile, mediaLink!!,
            object : ServiceApiClient.ServiceApiCallback {
                override fun onSuccess() {
                    runOnUiThread {
                        progressBar!!.visibility = View.GONE
                        buttonSendToService!!.isEnabled = true
                        buttonSendToService!!.text = "Send to $serviceTypeName"

                        Toast.makeText(
                            this@ShareActivity,
                            "Link sent successfully to $serviceTypeName!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Save to history
                        saveToHistory(mediaLink!!, profileId!!, profileNameFinal!!, serviceTypeName, true)

                        // Open browser with the service instance
                        openServiceInBrowser(profileUrl!!, profilePort)
                    }
                }

                override fun onError(error: String?) {
                    runOnUiThread {
                        progressBar!!.visibility = View.GONE
                        buttonSendToService!!.isEnabled = true
                        buttonSendToService!!.text = "Send to $serviceTypeName"

                        // Show error dialog instead of toast
                        DialogUtils.showErrorDialog(
                            this@ShareActivity,
                            R.string.error_sending_link_custom,
                            error ?: getString(R.string.error_sending_link_custom)
                        )

                        // Save to history with error status
                        saveToHistory(mediaLink!!, profileId!!, profileNameFinal!!, serviceTypeName, false)
                    }
                }
            })
    }

    /**
     * Share the link to other installed apps
     */
    private fun shareToApps() {
        if (mediaLink.isNullOrEmpty()) {
            Toast.makeText(this, R.string.no_youtube_link, Toast.LENGTH_SHORT).show()
            return
        }

        // Create a chooser intent to share the link
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, mediaLink)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Media Link")

        // Show chooser with app icons
        val chooserIntent = Intent.createChooser(shareIntent, "Share to Apps")
        startActivity(chooserIntent)

        // Save to history as shared to apps
        saveToHistory(mediaLink!!, "apps", "Other Apps", "Apps", true)
    }

    private fun openServiceInterface() {
        if (profiles.isEmpty()) {
            Toast.makeText(this, R.string.please_configure_profile_custom, Toast.LENGTH_SHORT).show()
            return
        }

        val selectedProfileText = autoCompleteProfiles!!.text.toString()
        var selectedProfile: ServerProfile? = null

        // Extract the profile name from the displayed text (name + service type)
        var profileName = selectedProfileText
        val parenIndex = selectedProfileText.indexOf(" (")
        if (parenIndex > 0) {
            profileName = selectedProfileText.substring(0, parenIndex)
        }

        for (profile in profiles) {
            if (profile.name == profileName) {
                selectedProfile = profile
                break
            }
        }

        if (selectedProfile == null) {
            Toast.makeText(this, R.string.please_configure_profile_custom, Toast.LENGTH_SHORT).show()
            return
        }

        // Store the profile URL and port for use in the callback
        val profileUrl = selectedProfile.url
        val profilePort = selectedProfile.port
        val profileId = selectedProfile.id
        val profileNameFinal = selectedProfile.name
        val serviceTypeName = selectedProfile.getServiceTypeName(this)

        // Save to history
        saveToHistory(mediaLink!!, profileId!!, profileNameFinal!!, serviceTypeName, true)

        // Open browser with the service instance
        openServiceInBrowser(profileUrl!!, profilePort)
    }

    private fun saveToHistory(
        url: String, profileId: String, profileName: String,
        serviceType: String, success: Boolean
    ) {
        // Create history item
        val historyItem = HistoryItem()
        historyItem.url = url

        // Use fetched metadata if available, otherwise fall back to simple extraction
        if (urlMetadata != null) {
            historyItem.title = urlMetadata?.title ?: extractTitleFromUrl(url)
            historyItem.description = urlMetadata?.description
            historyItem.thumbnailUrl = urlMetadata?.thumbnailUrl
            historyItem.serviceProvider = urlMetadata?.siteName ?: extractServiceProviderFromUrl(url)
        } else {
            historyItem.title = extractTitleFromUrl(url)
            historyItem.description = null
            historyItem.thumbnailUrl = null
            historyItem.serviceProvider = extractServiceProviderFromUrl(url)
        }

        historyItem.type = determineMediaType(url)
        historyItem.timestamp = System.currentTimeMillis()
        historyItem.profileId = profileId
        historyItem.profileName = profileName
        historyItem.isSentSuccessfully = success
        historyItem.serviceType = serviceType

        // Save to database
        val repository = HistoryRepository(this)
        repository.insertHistoryItem(historyItem)
    }

    private fun extractTitleFromUrl(url: String): String {
        // Simple title extraction - in a real app, you might fetch the actual title
        return url.replace("https://", "").replace("http://", "").replace("www.", "")
    }

    private fun extractServiceProviderFromUrl(url: String): String {
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "YouTube"
            url.contains("vimeo.com") -> "Vimeo"
            url.contains("twitch.tv") -> "Twitch"
            url.contains("reddit.com") -> "Reddit"
            url.contains("twitter.com") || url.contains("x.com") -> "Twitter"
            url.contains("instagram.com") -> "Instagram"
            url.contains("facebook.com") -> "Facebook"
            url.contains("soundcloud.com") -> "SoundCloud"
            url.contains("dailymotion.com") -> "Dailymotion"
            url.contains("bandcamp.com") -> "Bandcamp"
            url.startsWith("magnet:") -> "Magnet Link"
            else -> "Unknown"
        }
    }

    private fun determineMediaType(url: String): String {
        // Simple media type determination
        return when {
            url.contains("/playlist") || url.contains("&list=") -> "playlist"
            url.contains("/channel/") || url.contains("/user/") -> "channel"
            url.startsWith("magnet:") -> "torrent"
            else -> "single_video"
        }
    }

    private fun openServiceInBrowser(url: String, port: Int) {
        val fullUrl = "$url:$port"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
        startActivity(browserIntent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}