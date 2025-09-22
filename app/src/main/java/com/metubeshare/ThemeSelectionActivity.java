package com.metubeshare;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.metubeshare.database.Theme;
import com.metubeshare.database.ThemeRepository;
import java.util.List;

public class ThemeSelectionActivity extends AppCompatActivity implements ThemeAdapter.OnThemeSelectListener {
    private RecyclerView recyclerViewThemes;
    private ThemeAdapter themeAdapter;
    private ThemeRepository themeRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply current theme before setting content
        ThemeManager themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
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
        
        // Apply the theme
        ThemeManager themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this, theme);
        
        // Recreate the activity to apply the new theme
        recreate();
        
        // Finish the activity
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}