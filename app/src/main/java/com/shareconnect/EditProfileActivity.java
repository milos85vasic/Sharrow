package com.shareconnect;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText editTextProfileName;
    private TextInputEditText editTextServerUrl;
    private TextInputEditText editTextServerPort;
    private MaterialAutoCompleteTextView autoCompleteServiceType;
    private MaterialAutoCompleteTextView autoCompleteTorrentClient;
    private com.google.android.material.textfield.TextInputLayout layoutTorrentClient;
    private MaterialButton buttonCancel;
    private MaterialButton buttonSave;
    private MaterialButton buttonTestConnection;
    private ProfileManager profileManager;
    private ServerProfile existingProfile;
    private ServiceApiClient serviceApiClient;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileManager = new ProfileManager(this);
        serviceApiClient = new ServiceApiClient();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupListeners();
        setupSpinners();
        
        // Check if we're editing an existing profile
        String profileId = getIntent().getStringExtra("profile_id");
        if (profileId != null) {
            loadProfile(profileId);
        } else {
            // Set default values for new profile
            autoCompleteServiceType.setText("MeTube", false);
            layoutTorrentClient.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        editTextProfileName = findViewById(R.id.editTextProfileName);
        editTextServerUrl = findViewById(R.id.editTextServerUrl);
        editTextServerPort = findViewById(R.id.editTextServerPort);
        autoCompleteServiceType = findViewById(R.id.autoCompleteServiceType);
        autoCompleteTorrentClient = findViewById(R.id.autoCompleteTorrentClient);
        layoutTorrentClient = findViewById(R.id.layoutTorrentClient);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonSave);
        buttonTestConnection = findViewById(R.id.buttonTestConnection);
    }

    private void setupListeners() {
        buttonCancel.setOnClickListener(v -> finish());
        
        buttonSave.setOnClickListener(v -> saveProfile());
        
        buttonTestConnection.setOnClickListener(v -> testConnection());
        
        // Handle service type selection
        autoCompleteServiceType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedService = (String) parent.getItemAtPosition(position);
                handleServiceTypeChange(selectedService);
            }
        });
    }
    
    private void setupSpinners() {
        // Setup service type spinner
        List<String> serviceTypes = new ArrayList<>();
        serviceTypes.add("MeTube");
        serviceTypes.add("YT-DLP");
        serviceTypes.add("Torrent Client");
        serviceTypes.add("jDownloader");
        
        ArrayAdapter<String> serviceTypeAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, serviceTypes);
        autoCompleteServiceType.setAdapter(serviceTypeAdapter);
        
        // Setup torrent client spinner
        List<String> torrentClients = new ArrayList<>();
        torrentClients.add("qBittorrent");
        torrentClients.add("Transmission");
        torrentClients.add("uTorrent");
        
        ArrayAdapter<String> torrentClientAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, torrentClients);
        autoCompleteTorrentClient.setAdapter(torrentClientAdapter);
    }
    
    private void handleServiceTypeChange(String serviceType) {
        if ("Torrent Client".equals(serviceType)) {
            layoutTorrentClient.setVisibility(View.VISIBLE);
            // Set default torrent client
            autoCompleteTorrentClient.setText("qBittorrent", false);
        } else {
            layoutTorrentClient.setVisibility(View.GONE);
        }
    }

    private void loadProfile(String profileId) {
        // Load the existing profile
        for (ServerProfile profile : profileManager.getProfiles()) {
            if (profile.getId().equals(profileId)) {
                existingProfile = profile;
                break;
            }
        }
        
        if (existingProfile != null) {
            editTextProfileName.setText(existingProfile.getName());
            editTextServerUrl.setText(existingProfile.getUrl());
            editTextServerPort.setText(String.valueOf(existingProfile.getPort()));
            
            // Set service type
            switch (existingProfile.getServiceType()) {
                case ServerProfile.TYPE_METUBE:
                    autoCompleteServiceType.setText("MeTube", false);
                    break;
                case ServerProfile.TYPE_YTDL:
                    autoCompleteServiceType.setText("YT-DLP", false);
                    break;
                case ServerProfile.TYPE_TORRENT:
                    autoCompleteServiceType.setText("Torrent Client", false);
                    layoutTorrentClient.setVisibility(View.VISIBLE);
                    // Set torrent client type
                    switch (existingProfile.getTorrentClientType()) {
                        case ServerProfile.TORRENT_CLIENT_QBITTORRENT:
                            autoCompleteTorrentClient.setText("qBittorrent", false);
                            break;
                        case ServerProfile.TORRENT_CLIENT_TRANSMISSION:
                            autoCompleteTorrentClient.setText("Transmission", false);
                            break;
                        case ServerProfile.TORRENT_CLIENTUTORRENT:
                            autoCompleteTorrentClient.setText("uTorrent", false);
                            break;
                    }
                    break;
                case ServerProfile.TYPE_JDOWNLOADER:
                    autoCompleteServiceType.setText("jDownloader", false);
                    break;
            }
        }
    }

    private void saveProfile() {
        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            editTextProfileName.setError(getString(R.string.profile_name_required));
            return;
        }
        
        if (TextUtils.isEmpty(url)) {
            editTextServerUrl.setError(getString(R.string.url_required));
            return;
        }
        
        // Validate URL format
        if (!isValidUrl(url)) {
            editTextServerUrl.setError(getString(R.string.invalid_url));
            return;
        }

        if (TextUtils.isEmpty(portStr)) {
            editTextServerPort.setError(getString(R.string.invalid_port));
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                editTextServerPort.setError(getString(R.string.port_must_be_between));
                return;
            }
        } catch (NumberFormatException e) {
            editTextServerPort.setError(getString(R.string.invalid_port));
            return;
        }
        
        // Determine service type
        String serviceType;
        String torrentClientType = null;
        
        switch (serviceTypeStr) {
            case "MeTube":
                serviceType = ServerProfile.TYPE_METUBE;
                break;
            case "YT-DLP":
                serviceType = ServerProfile.TYPE_YTDL;
                break;
            case "Torrent Client":
                serviceType = ServerProfile.TYPE_TORRENT;
                // Determine torrent client type
                switch (torrentClientStr) {
                    case "qBittorrent":
                        torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT;
                        break;
                    case "Transmission":
                        torrentClientType = ServerProfile.TORRENT_CLIENT_TRANSMISSION;
                        break;
                    case "uTorrent":
                        torrentClientType = ServerProfile.TORRENT_CLIENTUTORRENT;
                        break;
                    default:
                        torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT; // Default
                        break;
                }
                break;
            case "jDownloader":
                serviceType = ServerProfile.TYPE_JDOWNLOADER;
                break;
            default:
                serviceType = ServerProfile.TYPE_METUBE; // Default fallback
                break;
        }

        // Create or update the profile
        if (existingProfile == null) {
            existingProfile = new ServerProfile();
            existingProfile.setId(java.util.UUID.randomUUID().toString());
        }
        
        existingProfile.setName(name);
        existingProfile.setUrl(url);
        existingProfile.setPort(port);
        existingProfile.setServiceType(serviceType);
        existingProfile.setTorrentClientType(torrentClientType);
        
        // Save the profile
        if (existingProfile.getId() == null || existingProfile.getId().isEmpty()) {
            profileManager.addProfile(existingProfile);
        } else {
            profileManager.updateProfile(existingProfile);
        }
        
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void testConnection() {
        String url = editTextServerUrl.getText().toString().trim();
        String portStr = editTextServerPort.getText().toString().trim();
        String serviceTypeStr = autoCompleteServiceType.getText().toString();
        String torrentClientStr = autoCompleteTorrentClient.getText().toString();

        // Validate URL format
        if (!isValidUrl(url)) {
            editTextServerUrl.setError(getString(R.string.invalid_url));
            return;
        }

        if (TextUtils.isEmpty(portStr)) {
            editTextServerPort.setError(getString(R.string.invalid_port));
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                editTextServerPort.setError(getString(R.string.port_must_be_between));
                return;
            }
        } catch (NumberFormatException e) {
            editTextServerPort.setError(getString(R.string.invalid_port));
            return;
        }
        
        // Determine service type
        String serviceType;
        String torrentClientType = null;
        
        switch (serviceTypeStr) {
            case "MeTube":
                serviceType = ServerProfile.TYPE_METUBE;
                break;
            case "YT-DLP":
                serviceType = ServerProfile.TYPE_YTDL;
                break;
            case "Torrent Client":
                serviceType = ServerProfile.TYPE_TORRENT;
                // Determine torrent client type
                switch (torrentClientStr) {
                    case "qBittorrent":
                        torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT;
                        break;
                    case "Transmission":
                        torrentClientType = ServerProfile.TORRENT_CLIENT_TRANSMISSION;
                        break;
                    case "uTorrent":
                        torrentClientType = ServerProfile.TORRENT_CLIENTUTORRENT;
                        break;
                    default:
                        torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT; // Default
                        break;
                }
                break;
            case "jDownloader":
                serviceType = ServerProfile.TYPE_JDOWNLOADER;
                break;
            default:
                serviceType = ServerProfile.TYPE_METUBE; // Default fallback
                break;
        }

        // Create a temporary profile for testing
        ServerProfile testProfile = new ServerProfile();
        testProfile.setUrl(url);
        testProfile.setPort(port);
        testProfile.setServiceType(serviceType);
        testProfile.setTorrentClientType(torrentClientType);

        // Show progress
        buttonTestConnection.setText("Testing...");
        buttonTestConnection.setEnabled(false);

        // Test with a simple URL (we'll use the root URL for testing)
        serviceApiClient.sendUrlToService(testProfile, "http://example.com", new ServiceApiClient.ServiceApiCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonTestConnection.setText("Test Connection");
                        buttonTestConnection.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, "Connection successful!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonTestConnection.setText("Test Connection");
                        buttonTestConnection.setEnabled(true);
                        // Show error dialog instead of toast
                        DialogUtils.showErrorDialog(EditProfileActivity.this, 
                            R.string.connection_error, 
                            error != null ? error : getString(R.string.error_sending_link_custom));
                    }
                });
            }
        });
    }

    private boolean isValidUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            return uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}