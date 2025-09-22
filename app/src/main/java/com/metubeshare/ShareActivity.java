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
    private TextView textViewMediaLink;
    private AutoCompleteTextView autoCompleteProfiles;
    private MaterialButton buttonSendToService;
    private MaterialButton buttonOpenService;
    private ProgressBar progressBar;
    private List<ServerProfile> profiles;
    private String mediaLink;
    private ServiceApiClient serviceApiClient;
    private ProfileManager profileManager;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
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
        serviceApiClient = new ServiceApiClient();
    }

    private void initViews() {
        textViewMediaLink = findViewById(R.id.textViewYouTubeLink);
        autoCompleteProfiles = findViewById(R.id.autoCompleteProfiles);
        buttonSendToService = findViewById(R.id.buttonSendToMeTube);
        buttonOpenService = findViewById(R.id.buttonOpenMeTube);
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
                textViewMediaLink.setText(mediaLink);
            }
        } else if (Intent.ACTION_VIEW.equals(action) && data != null) {
            // Handle direct URL intent
            mediaLink = data.toString();
            textViewMediaLink.setText(mediaLink);
        } else {
            // No valid link received
            mediaLink = null;
            textViewMediaLink.setText("No media link received");
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
            profileNames.add(profile.getName() + " (" + profile.getServiceTypeName() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, profileNames);
        autoCompleteProfiles.setAdapter(adapter);
        
        // Set default selection if there's a default profile
        ServerProfile defaultProfile = profileManager.getDefaultProfile();
        if (defaultProfile != null) {
            autoCompleteProfiles.setText(defaultProfile.getName() + " (" + defaultProfile.getServiceTypeName() + ")", false);
        } else if (!profileNames.isEmpty()) {
            // If no default, select the first one
            autoCompleteProfiles.setText(profileNames.get(0), false);
        }
    }

    private void setupListeners() {
        buttonSendToService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToService();
            }
        });
        
        buttonOpenService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openServiceInterface();
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
            openServiceInterface();
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

    private void sendToService() {
        if (mediaLink == null || mediaLink.isEmpty()) {
            Toast.makeText(this, R.string.no_youtube_link, Toast.LENGTH_SHORT).show();
            return;
        }

        if (profiles.isEmpty()) {
            Toast.makeText(this, R.string.please_configure_profile, Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedProfileText = autoCompleteProfiles.getText().toString();
        ServerProfile selectedProfile = null;
        
        // Extract the profile name from the displayed text (name + service type)
        String profileName = selectedProfileText;
        int parenIndex = selectedProfileText.indexOf(" (");
        if (parenIndex > 0) {
            profileName = selectedProfileText.substring(0, parenIndex);
        }
        
        for (ServerProfile profile : profiles) {
            if (profile.getName().equals(profileName)) {
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
        final String profileNameFinal = selectedProfile.getName();
        final String serviceTypeName = selectedProfile.getServiceTypeName();
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        buttonSendToService.setEnabled(false);
        buttonSendToService.setText("Sending to " + serviceTypeName + "...");
        
        // Call the service API
        serviceApiClient.sendUrlToService(selectedProfile, mediaLink, new ServiceApiClient.ServiceApiCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        buttonSendToService.setEnabled(true);
                        buttonSendToService.setText("Send to " + serviceTypeName);
                        
                        Toast.makeText(ShareActivity.this, "Link sent successfully to " + serviceTypeName + "!", Toast.LENGTH_SHORT).show();
                        
                        // Save to history
                        saveToHistory(mediaLink, profileId, profileNameFinal, serviceTypeName, true);
                        
                        // Open browser with the service instance
                        openServiceInBrowser(profileUrl, profilePort);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        buttonSendToService.setEnabled(true);
                        buttonSendToService.setText("Send to " + serviceTypeName);
                        
                        Toast.makeText(ShareActivity.this, "Error sending link to " + serviceTypeName + ": " + error, Toast.LENGTH_LONG).show();
                        
                        // Save to history with error status
                        saveToHistory(mediaLink, profileId, profileNameFinal, serviceTypeName, false);
                    }
                });
            }
        });
    }
    
    private void openServiceInterface() {
        if (profiles.isEmpty()) {
            Toast.makeText(this, R.string.please_configure_profile, Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedProfileText = autoCompleteProfiles.getText().toString();
        ServerProfile selectedProfile = null;
        
        // Extract the profile name from the displayed text (name + service type)
        String profileName = selectedProfileText;
        int parenIndex = selectedProfileText.indexOf(" (");
        if (parenIndex > 0) {
            profileName = selectedProfileText.substring(0, parenIndex);
        }
        
        for (ServerProfile profile : profiles) {
            if (profile.getName().equals(profileName)) {
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
        final String profileNameFinal = selectedProfile.getName();
        final String serviceTypeName = selectedProfile.getServiceTypeName();
        
        // Save to history
        saveToHistory(mediaLink, profileId, profileNameFinal, serviceTypeName, true);
        
        // Open browser with the service instance
        openServiceInBrowser(profileUrl, profilePort);
    }

    private void saveToHistory(String url, String profileId, String profileName, String serviceType, boolean success) {
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
        historyItem.setServiceType(serviceType); // Add service type to history
        
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
        } else if (url.startsWith("magnet:")) {
            return "Magnet Link";
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
        } else if (url.startsWith("magnet:")) {
            return "torrent";
        } else {
            return "single_video";
        }
    }
    
    private void openServiceInBrowser(String url, int port) {
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