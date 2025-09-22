package com.shareconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ProfilesActivity extends AppCompatActivity implements ProfileAdapter.OnProfileClickListener {
    private RecyclerView recyclerViewProfiles;
    private ProfileAdapter profileAdapter;
    private List<ServerProfile> profiles;
    private FloatingActionButton fabAddProfile;
    private TextView textViewNoProfiles;
    private ProfileManager profileManager;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        profileManager = new ProfileManager(this);

        initViews();
        setupRecyclerView();
        loadProfiles();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewProfiles = findViewById(R.id.recyclerViewProfiles);
        fabAddProfile = findViewById(R.id.fabAddProfile);
        textViewNoProfiles = findViewById(R.id.textViewNoProfiles);

        fabAddProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilesActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        profiles = new ArrayList<>();
        profileAdapter = new ProfileAdapter(profiles, this, profileManager);
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProfiles.setAdapter(profileAdapter);
    }

    private void loadProfiles() {
        profiles.clear();
        profiles.addAll(profileManager.getProfiles());
        profileAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (profiles.isEmpty()) {
            recyclerViewProfiles.setVisibility(View.GONE);
            textViewNoProfiles.setVisibility(View.VISIBLE);
        } else {
            recyclerViewProfiles.setVisibility(View.VISIBLE);
            textViewNoProfiles.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProfileClick(ServerProfile profile) {
        Intent intent = new Intent(ProfilesActivity.this, EditProfileActivity.class);
        intent.putExtra("profile_id", profile.getId());
        startActivity(intent);
    }

    @Override
    public void onSetDefaultClick(ServerProfile profile) {
        // Show confirmation dialog before setting default profile
        DialogUtils.showConfirmDialog(
            this,
            R.string.confirm_set_default,
            R.string.confirm_set_default_message,
            (dialog, which) -> {
                profileManager.setDefaultProfile(profile);
                loadProfiles(); // Refresh to show which is default
                dialog.dismiss();
            },
            null
        );
    }

    @Override
    public void onDeleteClick(ServerProfile profile) {
        // Show confirmation dialog before deleting profile
        DialogUtils.showConfirmDialog(
            this,
            R.string.confirm_delete_profile,
            R.string.confirm_delete_profile_message,
            (dialog, which) -> {
                profileManager.deleteProfile(profile);
                loadProfiles();
                dialog.dismiss();
            },
            null
        );
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
        // Refresh the list when returning from EditProfileActivity
        loadProfiles();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}