package com.metubeshare;

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

public class MainActivity extends AppCompatActivity {
    private MaterialButton buttonSettings;
    private MaterialButton buttonOpenMeTube;
    private ProfileManager profileManager;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content
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
}