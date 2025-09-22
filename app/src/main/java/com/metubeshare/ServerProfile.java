package com.metubeshare;

public class ServerProfile {
    private String id;
    private String name;
    private String url;
    private int port;
    private boolean isDefault;

    public ServerProfile() {
        // Default constructor required for serialization
    }

    public ServerProfile(String id, String name, String url, int port, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.port = port;
        this.isDefault = isDefault;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ServerProfile that = (ServerProfile) obj;
        return port == that.port &&
                isDefault == that.isDefault &&
                id.equals(that.id) &&
                name.equals(that.name) &&
                url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + port;
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }
}