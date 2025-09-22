package com.metubeshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.metubeshare.database.HistoryItem;
import com.metubeshare.database.HistoryRepository;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnHistoryItemClickListener {
    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private TextView textViewEmptyHistory;
    private AutoCompleteTextView autoCompleteServiceFilter;
    private AutoCompleteTextView autoCompleteTypeFilter;
    private AutoCompleteTextView autoCompleteServiceTypeFilter;
    private MaterialButton buttonClearFilters;
    
    private HistoryRepository historyRepository;
    private List<HistoryItem> allHistoryItems;
    private List<String> serviceProviders;
    private List<String> types;
    private List<String> serviceTypes;
    private ThemeManager themeManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        
        historyRepository = new HistoryRepository(this);
        loadHistoryItems();
    }
    
    private void initViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        textViewEmptyHistory = findViewById(R.id.textViewEmptyHistory);
        autoCompleteServiceFilter = findViewById(R.id.autoCompleteServiceFilter);
        autoCompleteTypeFilter = findViewById(R.id.autoCompleteTypeFilter);
        autoCompleteServiceTypeFilter = findViewById(R.id.autoCompleteServiceTypeFilter);
        buttonClearFilters = findViewById(R.id.buttonClearFilters);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(this);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);
    }
    
    private void setupFilters() {
        buttonClearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompleteServiceFilter.setText("");
                autoCompleteTypeFilter.setText("");
                autoCompleteServiceTypeFilter.setText("");
                loadHistoryItems();
            }
        });
    }
    
    private void loadHistoryItems() {
        allHistoryItems = historyRepository.getAllHistoryItems();
        serviceProviders = historyRepository.getAllServiceProviders();
        types = historyRepository.getAllTypes();
        serviceTypes = historyRepository.getAllServiceTypes();
        
        // Setup filter adapters
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, serviceProviders);
        autoCompleteServiceFilter.setAdapter(serviceAdapter);
        
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, types);
        autoCompleteTypeFilter.setAdapter(typeAdapter);
        
        ArrayAdapter<String> serviceTypeAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, serviceTypes);
        autoCompleteServiceTypeFilter.setAdapter(serviceTypeAdapter);
        
        // Apply filters if any
        String selectedService = autoCompleteServiceFilter.getText().toString();
        String selectedType = autoCompleteTypeFilter.getText().toString();
        String selectedServiceType = autoCompleteServiceTypeFilter.getText().toString();
        
        List<HistoryItem> filteredItems = new ArrayList<>(allHistoryItems);
        
        if (!selectedService.isEmpty()) {
            filteredItems.removeIf(item -> !item.getServiceProvider().equals(selectedService));
        }
        
        if (!selectedType.isEmpty()) {
            filteredItems.removeIf(item -> !item.getType().equals(selectedType));
        }
        
        if (!selectedServiceType.isEmpty()) {
            filteredItems.removeIf(item -> {
                String itemServiceType = item.getServiceType();
                if (itemServiceType == null) {
                    itemServiceType = "MeTube"; // Default for backward compatibility
                }
                return !itemServiceType.equals(selectedServiceType);
            });
        }
        
        historyAdapter.updateHistoryItems(filteredItems);
        updateUI();
    }
    
    private void updateUI() {
        if (historyAdapter.getItemCount() == 0) {
            recyclerViewHistory.setVisibility(View.GONE);
            textViewEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHistory.setVisibility(View.VISIBLE);
            textViewEmptyHistory.setVisibility(View.GONE);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_cleanup) {
            showCleanupDialog();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showCleanupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cleanup History");
        builder.setMessage("Choose what to cleanup:");
        
        String[] options = {"All History", "By Service Provider", "By Type", "By Service Type"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // All History
                    showConfirmDeleteAllDialog();
                    break;
                case 1: // By Service Provider
                    showCleanupByServiceProviderDialog();
                    break;
                case 2: // By Type
                    showCleanupByTypeDialog();
                    break;
                case 3: // By Service Type
                    showCleanupByServiceTypeDialog();
                    break;
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showConfirmDeleteAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All History");
        builder.setMessage("Are you sure you want to delete all history items?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            historyRepository.deleteAllHistoryItems();
            loadHistoryItems();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCleanupByServiceProviderDialog() {
        if (serviceProviders.isEmpty()) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete by Service Provider");
        builder.setItems(serviceProviders.toArray(new String[0]), (dialog, which) -> {
            String serviceProvider = serviceProviders.get(which);
            historyRepository.deleteHistoryItemsByServiceProvider(serviceProvider);
            loadHistoryItems();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCleanupByTypeDialog() {
        if (types.isEmpty()) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete by Type");
        builder.setItems(types.toArray(new String[0]), (dialog, which) -> {
            String type = types.get(which);
            historyRepository.deleteHistoryItemsByType(type);
            loadHistoryItems();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCleanupByServiceTypeDialog() {
        if (serviceTypes.isEmpty()) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete by Service Type");
        builder.setItems(serviceTypes.toArray(new String[0]), (dialog, which) -> {
            String serviceType = serviceTypes.get(which);
            historyRepository.deleteHistoryItemsByServiceType(serviceType);
            loadHistoryItems();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    public void onResendClick(HistoryItem item) {
        // Open share activity with the URL
        Intent intent = new Intent(this, ShareActivity.class);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, item.getUrl());
        startActivity(intent);
    }
    
    @Override
    public void onDeleteClick(HistoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete this history item?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            historyRepository.deleteHistoryItem(item);
            loadHistoryItems();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the history when returning to this activity
        loadHistoryItems();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}