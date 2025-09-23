package com.shareconnect

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {
    private var editTextProfileName: TextInputEditText? = null
    private var editTextServerUrl: TextInputEditText? = null
    private var editTextServerPort: TextInputEditText? = null
    private var autoCompleteServiceType: MaterialAutoCompleteTextView? = null
    private var autoCompleteTorrentClient: MaterialAutoCompleteTextView? = null
    private var layoutTorrentClient: com.google.android.material.textfield.TextInputLayout? = null
    private var buttonCancel: MaterialButton? = null
    private var buttonSave: MaterialButton? = null
    private var buttonTestConnection: MaterialButton? = null
    private var profileManager: ProfileManager? = null
    private var existingProfile: ServerProfile? = null
    private var serviceApiClient: ServiceApiClient? = null
    private var themeManager: ThemeManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this)
        themeManager!!.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileManager = ProfileManager(this)
        serviceApiClient = ServiceApiClient()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initViews()
        setupListeners()
        setupSpinners()

        // Check if we're editing an existing profile
        val profileId = intent.getStringExtra("profile_id")
        if (profileId != null) {
            loadProfile(profileId)
        } else {
            // Set default values for new profile
            autoCompleteServiceType!!.setText(getString(R.string.service_type_metube), false)
            layoutTorrentClient!!.visibility = View.GONE
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
    }

    private fun initViews() {
        editTextProfileName = findViewById(R.id.editTextProfileName)
        editTextServerUrl = findViewById(R.id.editTextServerUrl)
        editTextServerPort = findViewById(R.id.editTextServerPort)
        autoCompleteServiceType = findViewById(R.id.autoCompleteServiceType)
        autoCompleteTorrentClient = findViewById(R.id.autoCompleteTorrentClient)
        layoutTorrentClient = findViewById(R.id.layoutTorrentClient)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonSave = findViewById(R.id.buttonSave)
        buttonTestConnection = findViewById(R.id.buttonTestConnection)
    }

    private fun setupListeners() {
        buttonCancel!!.setOnClickListener { finish() }

        buttonSave!!.setOnClickListener { saveProfile() }

        buttonTestConnection!!.setOnClickListener { testConnection() }

        // Handle service type selection
        autoCompleteServiceType!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedService = autoCompleteServiceType!!.adapter.getItem(position) as String
                handleServiceTypeChange(selectedService)
            }
    }

    private fun setupSpinners() {
        // Setup service type spinner
        val serviceTypes: MutableList<String> = ArrayList()
        serviceTypes.add(getString(R.string.service_type_metube))
        serviceTypes.add(getString(R.string.service_type_ytdlp))
        serviceTypes.add(getString(R.string.service_type_torrent))
        serviceTypes.add(getString(R.string.service_type_jdownloader))

        val serviceTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, serviceTypes
        )
        autoCompleteServiceType!!.setAdapter(serviceTypeAdapter)

        // Setup torrent client spinner
        val torrentClients: MutableList<String> = ArrayList()
        torrentClients.add(getString(R.string.torrent_client_qbittorrent))
        torrentClients.add(getString(R.string.torrent_client_transmission))
        torrentClients.add(getString(R.string.torrent_client_utorrent))

        val torrentClientAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, torrentClients
        )
        autoCompleteTorrentClient!!.setAdapter(torrentClientAdapter)
    }

    private fun handleServiceTypeChange(serviceType: String) {
        if (getString(R.string.service_type_torrent) == serviceType) {
            layoutTorrentClient!!.visibility = View.VISIBLE
            // Set default torrent client
            autoCompleteTorrentClient!!.setText(getString(R.string.torrent_client_qbittorrent), false)
        } else {
            layoutTorrentClient!!.visibility = View.GONE
        }
    }

    private fun loadProfile(profileId: String) {
        // Load the existing profile
        for (profile in profileManager!!.profiles) {
            if (profile.id == profileId) {
                existingProfile = profile
                break
            }
        }

        if (existingProfile != null) {
            editTextProfileName!!.setText(existingProfile!!.name)
            editTextServerUrl!!.setText(existingProfile!!.url)
            editTextServerPort!!.setText(existingProfile!!.port.toString())

            // Set service type
            when (existingProfile!!.serviceType) {
                ServerProfile.TYPE_METUBE -> autoCompleteServiceType!!.setText(getString(R.string.service_type_metube), false)
                ServerProfile.TYPE_YTDL -> autoCompleteServiceType!!.setText(getString(R.string.service_type_ytdlp), false)
                ServerProfile.TYPE_TORRENT -> {
                    autoCompleteServiceType!!.setText(getString(R.string.service_type_torrent), false)
                    layoutTorrentClient!!.visibility = View.VISIBLE
                    // Set torrent client type
                    when (existingProfile!!.torrentClientType) {
                        ServerProfile.TORRENT_CLIENT_QBITTORRENT -> autoCompleteTorrentClient!!.setText(
                            getString(R.string.torrent_client_qbittorrent),
                            false
                        )
                        ServerProfile.TORRENT_CLIENT_TRANSMISSION -> autoCompleteTorrentClient!!.setText(
                            getString(R.string.torrent_client_transmission),
                            false
                        )
                        ServerProfile.TORRENT_CLIENTUTORRENT -> autoCompleteTorrentClient!!.setText(
                            getString(R.string.torrent_client_utorrent),
                            false
                        )
                    }
                }
                ServerProfile.TYPE_JDOWNLOADER -> autoCompleteServiceType!!.setText(getString(R.string.service_type_jdownloader), false)
            }
        }
    }

    private fun saveProfile() {
        // Get input values
        val name = editTextProfileName!!.text.toString().trim { it <= ' ' }
        val url = editTextServerUrl!!.text.toString().trim { it <= ' ' }
        val portStr = editTextServerPort!!.text.toString().trim { it <= ' ' }
        val serviceTypeStr = autoCompleteServiceType!!.text.toString()
        val torrentClientStr = autoCompleteTorrentClient!!.text.toString()

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            editTextProfileName!!.error = getString(R.string.profile_name_required)
            return
        }

        if (TextUtils.isEmpty(url)) {
            editTextServerUrl!!.error = getString(R.string.url_required)
            return
        }

        // Validate URL format
        if (!isValidUrl(url)) {
            editTextServerUrl!!.error = getString(R.string.invalid_url)
            return
        }

        if (TextUtils.isEmpty(portStr)) {
            editTextServerPort!!.error = getString(R.string.invalid_port)
            return
        }

        val port: Int
        try {
            port = portStr.toInt()
            if (port < 1 || port > 65535) {
                editTextServerPort!!.error = getString(R.string.port_must_be_between)
                return
            }
        } catch (e: NumberFormatException) {
            editTextServerPort!!.error = getString(R.string.invalid_port)
            return
        }

        // Determine service type
        val serviceType: String
        var torrentClientType: String? = null

        when (serviceTypeStr) {
            getString(R.string.service_type_metube) -> serviceType = ServerProfile.TYPE_METUBE
            getString(R.string.service_type_ytdlp) -> serviceType = ServerProfile.TYPE_YTDL
            getString(R.string.service_type_torrent) -> {
                serviceType = ServerProfile.TYPE_TORRENT
                // Determine torrent client type
                when (torrentClientStr) {
                    getString(R.string.torrent_client_qbittorrent) -> torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT
                    getString(R.string.torrent_client_transmission) -> torrentClientType = ServerProfile.TORRENT_CLIENT_TRANSMISSION
                    getString(R.string.torrent_client_utorrent) -> torrentClientType = ServerProfile.TORRENT_CLIENTUTORRENT
                    else -> torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT // Default
                }
            }
            getString(R.string.service_type_jdownloader) -> serviceType = ServerProfile.TYPE_JDOWNLOADER
            else -> serviceType = ServerProfile.TYPE_METUBE // Default fallback
        }

        // Check if we're creating a new profile or updating an existing one
        val isNewProfile = existingProfile == null

        // Create or update the profile
        if (isNewProfile) {
            existingProfile = ServerProfile()
            existingProfile!!.id = java.util.UUID.randomUUID().toString()
        }

        existingProfile!!.name = name
        existingProfile!!.url = url
        existingProfile!!.port = port
        existingProfile!!.serviceType = serviceType
        existingProfile!!.torrentClientType = torrentClientType

        // Save the profile
        if (isNewProfile) {
            profileManager!!.addProfile(existingProfile!!)
        } else {
            profileManager!!.updateProfile(existingProfile!!)
        }

        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun testConnection() {
        val url = editTextServerUrl!!.text.toString().trim { it <= ' ' }
        val portStr = editTextServerPort!!.text.toString().trim { it <= ' ' }
        val serviceTypeStr = autoCompleteServiceType!!.text.toString()
        val torrentClientStr = autoCompleteTorrentClient!!.text.toString()

        // Validate URL format
        if (!isValidUrl(url)) {
            editTextServerUrl!!.error = getString(R.string.invalid_url)
            return
        }

        if (TextUtils.isEmpty(portStr)) {
            editTextServerPort!!.error = getString(R.string.invalid_port)
            return
        }

        val port: Int
        try {
            port = portStr.toInt()
            if (port < 1 || port > 65535) {
                editTextServerPort!!.error = getString(R.string.port_must_be_between)
                return
            }
        } catch (e: NumberFormatException) {
            editTextServerPort!!.error = getString(R.string.invalid_port)
            return
        }

        // Determine service type
        val serviceType: String
        var torrentClientType: String? = null

        when (serviceTypeStr) {
            getString(R.string.service_type_metube) -> serviceType = ServerProfile.TYPE_METUBE
            getString(R.string.service_type_ytdlp) -> serviceType = ServerProfile.TYPE_YTDL
            getString(R.string.service_type_torrent) -> {
                serviceType = ServerProfile.TYPE_TORRENT
                // Determine torrent client type
                when (torrentClientStr) {
                    getString(R.string.torrent_client_qbittorrent) -> torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT
                    getString(R.string.torrent_client_transmission) -> torrentClientType = ServerProfile.TORRENT_CLIENT_TRANSMISSION
                    getString(R.string.torrent_client_utorrent) -> torrentClientType = ServerProfile.TORRENT_CLIENTUTORRENT
                    else -> torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT // Default
                }
            }
            getString(R.string.service_type_jdownloader) -> serviceType = ServerProfile.TYPE_JDOWNLOADER
            else -> serviceType = ServerProfile.TYPE_METUBE // Default fallback
        }

        // Create a temporary profile for testing
        val testProfile = ServerProfile()
        testProfile.url = url
        testProfile.port = port
        testProfile.serviceType = serviceType
        testProfile.torrentClientType = torrentClientType

        // Show progress
        buttonTestConnection!!.text = getString(R.string.testing)
        buttonTestConnection!!.isEnabled = false

        // Test with a simple URL (we'll use the root URL for testing)
        serviceApiClient!!.sendUrlToService(
            testProfile, "http://example.com",
            object : ServiceApiClient.ServiceApiCallback {
                override fun onSuccess() {
                    runOnUiThread {
                        buttonTestConnection!!.text = getString(R.string.test_connection)
                        buttonTestConnection!!.isEnabled = true
                        Toast.makeText(
                            this@EditProfileActivity,
                            R.string.connection_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(error: String?) {
                    runOnUiThread {
                        buttonTestConnection!!.text = getString(R.string.test_connection)
                        buttonTestConnection!!.isEnabled = true
                        // Show error dialog instead of toast
                        DialogUtils.showErrorDialog(
                            this@EditProfileActivity,
                            R.string.connection_error,
                            error ?: getString(R.string.error_sending_link_custom)
                        )
                    }
                }
            })
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https")
        } catch (e: Exception) {
            false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}