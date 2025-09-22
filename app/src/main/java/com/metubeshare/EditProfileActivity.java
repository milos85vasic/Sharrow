package com.metubeshare;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText editTextProfileName;
    private TextInputEditText editTextServerUrl;
    private TextInputEditText editTextServerPort;
    private MaterialButton buttonCancel;
    private MaterialButton buttonSave;
    private MaterialButton buttonTestConnection;
    private ProfileManager profileManager;
    private ServerProfile existingProfile;
    private MeTubeApiClient apiClient;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileManager = new ProfileManager(this);
        apiClient = new MeTubeApiClient();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupListeners();
        
        // Check if we're editing an existing profile
        String profileId = getIntent().getStringExtra("profile_id");
        if (profileId != null) {
            loadProfile(profileId);
        }
    }

    private void initViews() {
        editTextProfileName = findViewById(R.id.editTextProfileName);
        editTextServerUrl = findViewById(R.id.editTextServerUrl);
        editTextServerPort = findViewById(R.id.editTextServerPort);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonSave);
        buttonTestConnection = findViewById(R.id.buttonTestConnection);
    }

    private void setupListeners() {
        buttonCancel.setOnClickListener(v -> finish());
        
        buttonSave.setOnClickListener(v -> saveProfile());
        
        buttonTestConnection.setOnClickListener(v -> testConnection());
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
        }
    }

    private void saveProfile() {
        String name = editTextProfileName.getText().toString().trim();
        String url = editTextServerUrl.getText().toString().trim();
        String portStr = editTextServerPort.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextProfileName.setError("Profile name is required");
            return;
        }

        if (TextUtils.isEmpty(url)) {
            editTextServerUrl.setError("Server URL is required");
            return;
        }

        // Validate URL format
        if (!isValidUrl(url)) {
            editTextServerUrl.setError("Invalid URL format");
            return;
        }

        if (TextUtils.isEmpty(portStr)) {
            editTextServerPort.setError("Server port is required");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                editTextServerPort.setError("Port must be between 1 and 65535");
                return;
            }
        } catch (NumberFormatException e) {
            editTextServerPort.setError("Invalid port number");
            return;
        }

        // Create or update the profile
        if (existingProfile == null) {
            existingProfile = new ServerProfile();
        }
        
        existingProfile.setName(name);
        existingProfile.setUrl(url);
        existingProfile.setPort(port);
        
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

        // Validate URL format
        if (!isValidUrl(url)) {
            editTextServerUrl.setError("Invalid URL format");
            return;
        }

        if (TextUtils.isEmpty(portStr)) {
            editTextServerPort.setError("Server port is required");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                editTextServerPort.setError("Port must be between 1 and 65535");
                return;
            }
        } catch (NumberFormatException e) {
            editTextServerPort.setError("Invalid port number");
            return;
        }

        // Create a temporary profile for testing
        ServerProfile testProfile = new ServerProfile();
        testProfile.setUrl(url);
        testProfile.setPort(port);

        // Show progress
        buttonTestConnection.setText("Testing...");
        buttonTestConnection.setEnabled(false);

        // Test with a simple URL (we'll use the root URL for testing)
        apiClient.sendUrlToMeTube(testProfile, "http://example.com", new MeTubeApiClient.MeTubeApiCallback() {
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
                        Toast.makeText(EditProfileActivity.this, "Connection failed: " + error, Toast.LENGTH_LONG).show();
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