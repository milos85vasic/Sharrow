package com.metubeshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.metubeshare.database.HistoryItem;
import com.metubeshare.database.HistoryRepository;
import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends AppCompatActivity {
    private TextView textViewYouTubeLink;
    private AutoCompleteTextView autoCompleteProfiles;
    private MaterialButton buttonSendToMeTube;
    private MaterialButton buttonOpenMeTube;
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        buttonOpenMeTube = findViewById(R.id.buttonOpenMeTube);
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
        
        buttonOpenMeTube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMeTubeInterface();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_open_metube) {
            openMeTubeInterface();
            return true;
        } else if (id == R.id.action_history) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        final String profileId = selectedProfile.getId();
        final String profileName = selectedProfile.getName();
        
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
                        
                        // Save to history
                        saveToHistory(mediaLink, profileId, profileName, true);
                        
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
                        
                        // Save to history with error status
                        saveToHistory(mediaLink, profileId, profileName, false);
                    }
                });
            }
        });
    }
    
    private void saveToHistory(String url, String profileId, String profileName, boolean success) {
        // Create history item
        HistoryItem historyItem = new HistoryItem();
        historyItem.setUrl(url);
        historyItem.setTitle(extractTitleFromUrl(url)); // Simple title extraction
        historyItem.setServiceProvider(extractServiceProviderFromUrl(url));
        historyItem.setType(determineMediaType(url));
        historyItem.setTimestamp(System.currentTimeMillis());
        historyItem.setProfileId(profileId);
        historyItem.setProfileName(profileName);
        historyItem.setSentSuccessfully(success);
        
        // Save to database
        HistoryRepository repository = new HistoryRepository(this);
        repository.insertHistoryItem(historyItem);
    }
    
    private String extractTitleFromUrl(String url) {
        // Simple title extraction - in a real app, you might fetch the actual title
        return url.replace("https://", "").replace("http://", "").replace("www.", "");
    }
    
    private String extractServiceProviderFromUrl(String url) {
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return "YouTube";
        } else if (url.contains("vimeo.com")) {
            return "Vimeo";
        } else if (url.contains("twitch.tv")) {
            return "Twitch";
        } else if (url.contains("reddit.com")) {
            return "Reddit";
        } else if (url.contains("twitter.com") || url.contains("x.com")) {
            return "Twitter";
        } else if (url.contains("instagram.com")) {
            return "Instagram";
        } else if (url.contains("facebook.com")) {
            return "Facebook";
        } else if (url.contains("soundcloud.com")) {
            return "SoundCloud";
        } else if (url.contains("dailymotion.com")) {
            return "Dailymotion";
        } else if (url.contains("bandcamp.com")) {
            return "Bandcamp";
        } else {
            return "Unknown";
        }
    }
    
    private String determineMediaType(String url) {
        // Simple media type determination
        if (url.contains("/playlist") || url.contains("&list=")) {
            return "playlist";
        } else if (url.contains("/channel/") || url.contains("/user/")) {
            return "channel";
        } else {
            return "single_video";
        }
    }
    
    private void openMeTubeInterface() {
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
        
        // Save to history
        saveToHistory(mediaLink, selectedProfile.getId(), selectedProfile.getName(), true);
        
        // Open browser with the MeTube instance
        openMeTubeInBrowser(profileUrl, profilePort);
    }

    private void openMeTubeInBrowser(String url, int port) {
        String fullUrl = url + ":" + port;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
        startActivity(browserIntent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}