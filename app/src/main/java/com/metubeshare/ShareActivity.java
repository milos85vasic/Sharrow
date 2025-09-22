package com.metubeshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends AppCompatActivity {
    private TextView textViewYouTubeLink;
    private AutoCompleteTextView autoCompleteProfiles;
    private MaterialButton buttonSendToMeTube;
    private ProgressBar progressBar;
    private List<ServerProfile> profiles;
    private String mediaLink;
    private MeTubeApiClient apiClient;
    private ProfileManager profileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        profileManager = new ProfileManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        handleIntent();
        loadProfiles();
        setupListeners();
        apiClient = new MeTubeApiClient();
    }

    private void initViews() {
        textViewYouTubeLink = findViewById(R.id.textViewYouTubeLink);
        autoCompleteProfiles = findViewById(R.id.autoCompleteProfiles);
        buttonSendToMeTube = findViewById(R.id.buttonSendToMeTube);
        progressBar = findViewById(R.id.progressBar);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri data = intent.getData();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            // Handle shared text (URL)
            mediaLink = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (mediaLink != null) {
                textViewYouTubeLink.setText(mediaLink);
            }
        } else if (Intent.ACTION_VIEW.equals(action) && data != null) {
            // Handle direct URL intent
            mediaLink = data.toString();
            textViewYouTubeLink.setText(mediaLink);
        } else {
            // No valid link received
            mediaLink = null;
            textViewYouTubeLink.setText("No media link received");
        }
    }

    private void loadProfiles() {
        profiles = profileManager.getProfiles();
        
        if (profiles.isEmpty()) {
            // Show a message and redirect to settings
            Toast.makeText(this, R.string.please_configure_profile, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Create adapter for AutoCompleteTextView
        List<String> profileNames = new ArrayList<>();
        for (ServerProfile profile : profiles) {
            profileNames.add(profile.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, profileNames);
        autoCompleteProfiles.setAdapter(adapter);
        
        // Set default selection if there's a default profile
        ServerProfile defaultProfile = profileManager.getDefaultProfile();
        if (defaultProfile != null) {
            autoCompleteProfiles.setText(defaultProfile.getName(), false);
        } else if (!profileNames.isEmpty()) {
            // If no default, select the first one
            autoCompleteProfiles.setText(profileNames.get(0), false);
        }
    }

    private void setupListeners() {
        buttonSendToMeTube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToMeTube();
            }
        });
    }

    private void sendToMeTube() {
        if (mediaLink == null || mediaLink.isEmpty()) {
            Toast.makeText(this, R.string.no_youtube_link, Toast.LENGTH_SHORT).show();
            return;
        }

        if (profiles.isEmpty()) {
            Toast.makeText(this, R.string.please_configure_profile, Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedProfileName = autoCompleteProfiles.getText().toString();
        ServerProfile selectedProfile = null;
        
        for (ServerProfile profile : profiles) {
            if (profile.getName().equals(selectedProfileName)) {
                selectedProfile = profile;
                break;
            }
        }
        
        if (selectedProfile == null) {
            Toast.makeText(this, R.string.please_configure_profile, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Store the profile URL and port for use in the callback
        final String profileUrl = selectedProfile.getUrl();
        final int profilePort = selectedProfile.getPort();
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        buttonSendToMeTube.setEnabled(false);
        
        // Call the MeTube API
        apiClient.sendUrlToMeTube(selectedProfile, mediaLink, new MeTubeApiClient.MeTubeApiCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        buttonSendToMeTube.setEnabled(true);
                        
                        Toast.makeText(ShareActivity.this, R.string.sent_successfully, Toast.LENGTH_SHORT).show();
                        
                        // Open browser with the MeTube instance
                        openMeTubeInBrowser(profileUrl, profilePort);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        buttonSendToMeTube.setEnabled(true);
                        
                        Toast.makeText(ShareActivity.this, R.string.error_sending_link + ": " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void openMeTubeInBrowser(String url, int port) {
        String fullUrl = url + ":" + port;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
        startActivity(browserIntent);
        
        // Finish this activity
        finish();
    }
}