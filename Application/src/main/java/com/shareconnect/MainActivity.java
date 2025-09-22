package com.shareconnect;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private MaterialButton buttonSettings;
    private MaterialButton buttonOpenMeTube;
    private ProfileManager profileManager;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content and calling super.onCreate()
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        
        profileManager = new ProfileManager(this);
        
        // Check if we have any profiles configured
        if (!profileManager.hasProfiles()) {
            // Show setup wizard
            showSetupWizard();
            return;
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonSettings = findViewById(R.id.buttonSettings);
        buttonOpenMeTube = findViewById(R.id.buttonOpenMeTube);
        MaterialButton buttonHistory = findViewById(R.id.buttonHistory);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        
        buttonOpenMeTube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMeTubeInterface();
            }
        });
        
        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory();
            }
        });
        
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddFromClipboard();
            }
        });
    }

    private void showSetupWizard() {
        // For now, we'll just redirect to settings
        // In a more complete implementation, we could show a guided setup wizard
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            openSettings();
            return true;
        } else if (id == R.id.action_open_metube) {
            openMeTubeInterface();
            return true;
        } else if (id == R.id.action_history) {
            openHistory();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    private void openHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    
    private void openMeTubeInterface() {
        ServerProfile defaultProfile = profileManager.getDefaultProfile();
        
        if (defaultProfile == null) {
            Toast.makeText(this, "Please set a default profile in Settings", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String url = defaultProfile.getUrl() + ":" + defaultProfile.getPort();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
    
    /**
     * Handle adding a URL from clipboard
     */
    private void handleAddFromClipboard() {
        // Get clipboard manager
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        
        // Check if clipboard has primary clip
        if (clipboard.hasPrimaryClip()) {
            ClipData clipData = clipboard.getPrimaryClip();
            
            // Check if clip data is not null and has at least one item
            if (clipData != null && clipData.getItemCount() > 0) {
                // Get the text from the first item
                CharSequence clipboardText = clipData.getItemAt(0).getText();
                
                if (clipboardText != null) {
                    String url = clipboardText.toString().trim();
                    
                    // Validate URL
                    if (isValidUrl(url)) {
                        // Open ShareActivity with the URL from clipboard
                        Intent intent = new Intent(this, ShareActivity.class);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, url);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Invalid URL in clipboard", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No text found in clipboard", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Simple URL validation
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        // Check if it starts with http:// or https://
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }
        
        // Basic validation - check if it contains a domain
        try {
            Uri uri = Uri.parse(url);
            return uri.getHost() != null && !uri.getHost().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check if theme has changed and recreate activity if needed
        themeManager = ThemeManager.getInstance(this);
        if (themeManager.hasThemeChanged()) {
            themeManager.resetThemeChangedFlag();
            recreate();
        }
    }
}