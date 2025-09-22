package com.shareconnect;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.shareconnect.database.Theme;
import com.shareconnect.database.ThemeRepository;
import java.util.List;

public class ThemeSelectionActivity extends AppCompatActivity implements ThemeAdapter.OnThemeSelectListener {
    private RecyclerView recyclerViewThemes;
    private ThemeAdapter themeAdapter;
    private ThemeRepository themeRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply current theme before setting content and calling super.onCreate()
        ThemeManager themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_theme_selection);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        
        themeRepository = themeManager.getThemeRepository();
        loadThemes();
    }
    
    private void initViews() {
        recyclerViewThemes = findViewById(R.id.recyclerViewThemes);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        themeAdapter = new ThemeAdapter(this);
        recyclerViewThemes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewThemes.setAdapter(themeAdapter);
    }
    
    private void loadThemes() {
        List<Theme> themes = themeRepository.getAllThemes();
        themeAdapter.updateThemes(themes);
    }
    
    @Override
    public void onThemeSelected(Theme theme) {
        // Set this theme as default
        themeRepository.setDefaultTheme(theme.getId());
        
        // Debug: Log the selected theme
        android.util.Log.d("ThemeSelection", "Selected theme: " + theme.getName() + " (ID: " + theme.getId() + ")");
        
        // Notify that theme has changed
        ThemeManager themeManager = ThemeManager.getInstance(this);
        themeManager.notifyThemeChanged();
        
        // Debug: Log that theme change was notified
        android.util.Log.d("ThemeSelection", "Theme change notified");
        
        // Set result to indicate theme was changed
        setResult(RESULT_OK);
        
        // Finish the activity
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}