package com.shareconnect

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.shareconnect.database.HistoryItem
import com.shareconnect.database.HistoryRepository

class HistoryActivity : AppCompatActivity(), HistoryAdapter.OnHistoryItemClickListener {
    private var recyclerViewHistory: RecyclerView? = null
    private var historyAdapter: HistoryAdapter? = null
    private var textViewEmptyHistory: TextView? = null
    private var autoCompleteServiceFilter: AutoCompleteTextView? = null
    private var autoCompleteTypeFilter: AutoCompleteTextView? = null
    private var autoCompleteServiceTypeFilter: AutoCompleteTextView? = null
    private var buttonClearFilters: MaterialButton? = null

    private var historyRepository: HistoryRepository? = null
    private var allHistoryItems: List<HistoryItem> = ArrayList()
    private var serviceProviders: List<String> = ArrayList()
    private var types: List<String> = ArrayList()
    private var serviceTypes: List<String> = ArrayList()
    private var themeManager: ThemeManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this)
        themeManager!!.applyTheme(this)

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContentView(R.layout.activity_history)

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupFilters()

        historyRepository = HistoryRepository(this)
        loadHistoryItems()
    }

    private fun initViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)
        textViewEmptyHistory = findViewById(R.id.textViewEmptyHistory)
        autoCompleteServiceFilter = findViewById(R.id.autoCompleteServiceFilter)
        autoCompleteTypeFilter = findViewById(R.id.autoCompleteTypeFilter)
        autoCompleteServiceTypeFilter = findViewById(R.id.autoCompleteServiceTypeFilter)
        buttonClearFilters = findViewById(R.id.buttonClearFilters)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(this)
        recyclerViewHistory!!.layoutManager = LinearLayoutManager(this)
        recyclerViewHistory!!.adapter = historyAdapter
    }

    private fun setupFilters() {
        buttonClearFilters!!.setOnClickListener {
            autoCompleteServiceFilter!!.setText("")
            autoCompleteTypeFilter!!.setText("")
            autoCompleteServiceTypeFilter!!.setText("")
            loadHistoryItems()
        }
    }

    private fun loadHistoryItems() {
        allHistoryItems = historyRepository!!.allHistoryItems
        serviceProviders = historyRepository!!.allServiceProviders
        types = historyRepository!!.allTypes
        serviceTypes = historyRepository!!.allServiceTypes

        // Setup filter adapters
        val serviceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, serviceProviders
        )
        autoCompleteServiceFilter!!.setAdapter(serviceAdapter)

        val typeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, types
        )
        autoCompleteTypeFilter!!.setAdapter(typeAdapter)

        val serviceTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, serviceTypes
        )
        autoCompleteServiceTypeFilter!!.setAdapter(serviceTypeAdapter)

        // Apply filters if any
        val selectedService = autoCompleteServiceFilter!!.text.toString()
        val selectedType = autoCompleteTypeFilter!!.text.toString()
        val selectedServiceType = autoCompleteServiceTypeFilter!!.text.toString()

        val filteredItems = ArrayList(allHistoryItems)

        if (selectedService.isNotEmpty()) {
            filteredItems.removeIf { item -> item.serviceProvider != selectedService }
        }

        if (selectedType.isNotEmpty()) {
            filteredItems.removeIf { item -> item.type != selectedType }
        }

        if (selectedServiceType.isNotEmpty()) {
            filteredItems.removeIf { item ->
                var itemServiceType = item.serviceType
                if (itemServiceType == null) {
                    itemServiceType = "MeTube" // Default for backward compatibility
                }
                itemServiceType != selectedServiceType
            }
        }

        historyAdapter!!.updateHistoryItems(filteredItems)
        updateUI()
    }

    private fun updateUI() {
        if (historyAdapter!!.itemCount == 0) {
            recyclerViewHistory!!.visibility = View.GONE
            textViewEmptyHistory!!.visibility = View.VISIBLE
        } else {
            recyclerViewHistory!!.visibility = View.VISIBLE
            textViewEmptyHistory!!.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.action_cleanup -> {
                showCleanupDialog()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showCleanupDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cleanup History")
        builder.setMessage("Choose what to cleanup:")

        val options = arrayOf(
            getString(R.string.all_history),
            getString(R.string.by_service_provider),
            getString(R.string.by_type),
            getString(R.string.by_service_type)
        )
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showConfirmDeleteAllDialog()
                1 -> showCleanupByServiceProviderDialog()
                2 -> showCleanupByTypeDialog()
                3 -> showCleanupByServiceTypeDialog()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showConfirmDeleteAllDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete All History")
        builder.setMessage("Are you sure you want to delete all history items?")
        builder.setPositiveButton("Delete") { _, _ ->
            historyRepository!!.deleteAllHistoryItems()
            loadHistoryItems()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showCleanupByServiceProviderDialog() {
        if (serviceProviders.isEmpty()) {
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete by Service Provider")
        builder.setItems(serviceProviders.toTypedArray()) { _, which ->
            val serviceProvider = serviceProviders[which]
            historyRepository!!.deleteHistoryItemsByServiceProvider(serviceProvider)
            loadHistoryItems()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showCleanupByTypeDialog() {
        if (types.isEmpty()) {
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete by Type")
        builder.setItems(types.toTypedArray()) { _, which ->
            val type = types[which]
            historyRepository!!.deleteHistoryItemsByType(type)
            loadHistoryItems()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showCleanupByServiceTypeDialog() {
        if (serviceTypes.isEmpty()) {
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete by Service Type")
        builder.setItems(serviceTypes.toTypedArray()) { _, which ->
            val serviceType = serviceTypes[which]
            historyRepository!!.deleteHistoryItemsByServiceType(serviceType)
            loadHistoryItems()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun onResendClick(item: HistoryItem) {
        // Open share activity with the URL
        val intent = Intent(this, ShareActivity::class.java)
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, item.url)
        startActivity(intent)
    }

    override fun onDeleteClick(item: HistoryItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Item")
        builder.setMessage("Are you sure you want to delete this history item?")
        builder.setPositiveButton("Delete") { _, _ ->
            historyRepository!!.deleteHistoryItem(item)
            loadHistoryItems()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        // Check if theme has changed and recreate activity if needed
        themeManager = ThemeManager.getInstance(this)
        if (themeManager!!.hasThemeChanged()) {
            themeManager!!.resetThemeChangedFlag()
            recreate()
        }
        // Refresh the history when returning to this activity
        loadHistoryItems()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}