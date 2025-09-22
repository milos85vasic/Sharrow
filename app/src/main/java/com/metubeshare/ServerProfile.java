package com.metubeshare;

public class ServerProfile {
    public static final String TYPE_METUBE = "metube";
    public static final String TYPE_YTDL = "ytdl";
    public static final String TYPE_TORRENT = "torrent";
    public static final String TYPE_JDOWNLOADER = "jdownloader";
    
    public static final String TORRENT_CLIENT_QBITTORRENT = "qbittorrent";
    public static final String TORRENT_CLIENT_TRANSMISSION = "transmission";
    public static final String TORRENT_CLIENTUTORRENT = "utorrent";
    
    private String id;
    private String name;
    private String url;
    private int port;
    private boolean isDefault;
    private String serviceType; // metube, ytdl, torrent, jdownloader
    private String torrentClientType; // qbittorrent, transmission, utorrent (only for torrent type)
    
    public ServerProfile() {
        // Default constructor required for serialization
        this.serviceType = TYPE_METUBE; // Default to MeTube for backward compatibility
    }

    public ServerProfile(String id, String name, String url, int port, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.port = port;
        this.isDefault = isDefault;
        this.serviceType = TYPE_METUBE; // Default to MeTube for backward compatibility
    }
    
    public ServerProfile(String id, String name, String url, int port, boolean isDefault, 
                         String serviceType, String torrentClientType) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.port = port;
        this.isDefault = isDefault;
        this.serviceType = serviceType;
        this.torrentClientType = torrentClientType;
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
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getTorrentClientType() {
        return torrentClientType;
    }
    
    public void setTorrentClientType(String torrentClientType) {
        this.torrentClientType = torrentClientType;
    }
    
    public boolean isMeTube() {
        return TYPE_METUBE.equals(serviceType);
    }
    
    public boolean isYtDl() {
        return TYPE_YTDL.equals(serviceType);
    }
    
    public boolean isTorrent() {
        return TYPE_TORRENT.equals(serviceType);
    }
    
    public boolean isJDownloader() {
        return TYPE_JDOWNLOADER.equals(serviceType);
    }
    
    public String getServiceTypeName() {
        switch (serviceType) {
            case TYPE_METUBE:
                return "MeTube";
            case TYPE_YTDL:
                return "YT-DLP";
            case TYPE_TORRENT:
                return "Torrent (" + getTorrentClientName() + ")";
            case TYPE_JDOWNLOADER:
                return "jDownloader";
            default:
                return "Unknown";
        }
    }
    
    public String getTorrentClientName() {
        if (torrentClientType == null) {
            return "Unknown";
        }
        
        switch (torrentClientType) {
            case TORRENT_CLIENT_QBITTORRENT:
                return "qBittorrent";
            case TORRENT_CLIENT_TRANSMISSION:
                return "Transmission";
            case TORRENT_CLIENTUTORRENT:
                return "uTorrent";
            default:
                return torrentClientType;
        }
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
                url.equals(that.url) &&
                serviceType.equals(that.serviceType) &&
                (torrentClientType == null ? that.torrentClientType == null : torrentClientType.equals(that.torrentClientType));
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + port;
        result = 31 * result + (isDefault ? 1 : 0);
        result = 31 * result + serviceType.hashCode();
        result = 31 * result + (torrentClientType == null ? 0 : torrentClientType.hashCode());
        return result;
    }
}