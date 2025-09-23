package com.shareconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.redelf.commons.logging.Console
import com.shareconnect.database.Theme

class ThemeSelectionActivity : AppCompatActivity(), ThemeAdapter.OnThemeSelectListener {
    private var recyclerViewThemes: RecyclerView? = null
    private var themeAdapter: ThemeAdapter? = null
    private var themeRepository: com.shareconnect.database.ThemeRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply current theme before setting content and calling super.onCreate()
        val themeManager = ThemeManager.getInstance(this)
        themeManager.applyTheme(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_theme_selection)

        initViews()
        setupToolbar()
        setupRecyclerView()

        themeRepository = themeManager.themeRepositoryVal
        loadThemes()
    }

    private fun initViews() {
        recyclerViewThemes = findViewById(R.id.recyclerViewThemes)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        themeAdapter = ThemeAdapter(this)
        recyclerViewThemes!!.layoutManager = LinearLayoutManager(this)
        recyclerViewThemes!!.adapter = themeAdapter
    }

    private fun loadThemes() {
        val themes = themeRepository!!.allThemes
        themeAdapter!!.updateThemes(themes)
    }

    override fun onThemeSelected(theme: Theme) {
        // Set this theme as default
        Console.debug("onThemeSelected() called with theme: " + theme.name + " (ID: " + theme.id + ", isDefault: " + theme.isDefault + ")")
        themeRepository!!.setDefaultTheme(theme.id)

        // Debug: Log the selected theme
        Console.debug("Selected theme: " + theme.name + " (ID: " + theme.id + ")")

        // Set result to indicate theme was changed
        setResult(RESULT_OK)

        // Finish the activity immediately
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}