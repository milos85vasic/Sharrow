package com.shareconnect.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "history_items")
@TypeConverters({Converters.class})
public class HistoryItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String url;
    private String title;
    private String serviceProvider;
    private String type; // single_video, playlist, channel
    private long timestamp;
    private String profileId;
    private String profileName;
    private boolean sentSuccessfully;
    private String serviceType; // MeTube, Torrent, jDownloader
    
    // Constructors
    public HistoryItem() {}
    
    public HistoryItem(String url, String title, String serviceProvider, String type, 
                      long timestamp, String profileId, String profileName, boolean sentSuccessfully,
                      String serviceType) {
        this.url = url;
        this.title = title;
        this.serviceProvider = serviceProvider;
        this.type = type;
        this.timestamp = timestamp;
        this.profileId = profileId;
        this.profileName = profileName;
        this.sentSuccessfully = sentSuccessfully;
        this.serviceType = serviceType;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getServiceProvider() {
        return serviceProvider;
    }
    
    public void setServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getProfileId() {
        return profileId;
    }
    
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
    
    public String getProfileName() {
        return profileName;
    }
    
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    
    public boolean isSentSuccessfully() {
        return sentSuccessfully;
    }
    
    public void setSentSuccessfully(boolean sentSuccessfully) {
        this.sentSuccessfully = sentSuccessfully;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}