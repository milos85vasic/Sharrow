package com.shareconnect;

import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class ServiceApiClient {
    private static final String TAG = "ServiceApiClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    
    public ServiceApiClient() {
        client = new OkHttpClient();
    }
    
    public interface ServiceApiCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * Send URL to the appropriate service based on profile type
     */
    public void sendUrlToService(ServerProfile profile, String url, ServiceApiCallback callback) {
        switch (profile.getServiceType()) {
            case ServerProfile.TYPE_METUBE:
                sendUrlToMeTube(profile, url, callback);
                break;
            case ServerProfile.TYPE_YTDL:
                sendUrlToYtdl(profile, url, callback);
                break;
            case ServerProfile.TYPE_TORRENT:
                sendUrlToTorrentClient(profile, url, callback);
                break;
            case ServerProfile.TYPE_JDOWNLOADER:
                sendUrlToJDownloader(profile, url, callback);
                break;
            default:
                callback.onError("Unsupported service type: " + profile.getServiceType());
                break;
        }
    }
    
    /**
     * Send URL to MeTube service
     */
    private void sendUrlToMeTube(ServerProfile profile, String youtubeUrl, ServiceApiCallback callback) {
        try {
            // Construct the MeTube API endpoint
            String url = profile.getUrl() + ":" + profile.getPort() + "/add";
            
            // Create the JSON payload
            JSONObject json = new JSONObject();
            json.put("url", youtubeUrl);
            json.put("quality", "best"); // Default to best quality
            
            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send URL to MeTube", e);
                    callback.onError(e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "MeTube API error: " + errorBody);
                            callback.onError("API Error: " + response.code() + " - " + errorBody);
                        }
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing MeTube API request", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send URL to YT-DLP service
     */
    private void sendUrlToYtdl(ServerProfile profile, String url, ServiceApiCallback callback) {
        try {
            // Construct the YT-DLP API endpoint (similar to MeTube)
            String apiUrl = profile.getUrl() + ":" + profile.getPort() + "/add";
            
            // Create the JSON payload
            JSONObject json = new JSONObject();
            json.put("url", url);
            json.put("quality", "best"); // Default to best quality
            
            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send URL to YT-DLP", e);
                    callback.onError(e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "YT-DLP API error: " + errorBody);
                            callback.onError("API Error: " + response.code() + " - " + errorBody);
                        }
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing YT-DLP API request", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send URL to torrent client
     */
    private void sendUrlToTorrentClient(ServerProfile profile, String url, ServiceApiCallback callback) {
        try {
            String baseUrl = profile.getUrl() + ":" + profile.getPort();
            
            // Check if it's a magnet link or regular URL
            if (url.startsWith("magnet:")) {
                // For magnet links, send directly to torrent client
                sendMagnetToTorrentClient(profile, url, callback);
            } else {
                // For regular URLs, we might need to handle differently depending on the client
                sendRegularUrlToTorrentClient(profile, url, callback);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error preparing torrent client request", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send regular URL to torrent client
     */
    private void sendRegularUrlToTorrentClient(ServerProfile profile, String url, ServiceApiCallback callback) {
        try {
            String baseUrl = profile.getUrl() + ":" + profile.getPort();
            String apiUrl = "";
            
            // Different torrent clients have different APIs
            switch (profile.getTorrentClientType()) {
                case ServerProfile.TORRENT_CLIENT_QBITTORRENT:
                    // qBittorrent Web API
                    apiUrl = baseUrl + "/api/v2/torrents/add";
                    sendToQBittorrent(apiUrl, url, callback);
                    break;
                    
                case ServerProfile.TORRENT_CLIENT_TRANSMISSION:
                    // Transmission RPC API
                    apiUrl = baseUrl + "/transmission/rpc";
                    sendToTransmission(apiUrl, url, callback);
                    break;
                    
                case ServerProfile.TORRENT_CLIENTUTORRENT:
                    // uTorrent Web API
                    apiUrl = baseUrl + "/gui/";
                    sendToUTorrent(apiUrl, url, callback);
                    break;
                    
                default:
                    callback.onError("Unsupported torrent client: " + profile.getTorrentClientType());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending URL to torrent client", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send magnet link to torrent client
     */
    private void sendMagnetToTorrentClient(ServerProfile profile, String magnetUrl, ServiceApiCallback callback) {
        try {
            String baseUrl = profile.getUrl() + ":" + profile.getPort();
            String apiUrl = "";
            
            // Different torrent clients have different APIs
            switch (profile.getTorrentClientType()) {
                case ServerProfile.TORRENT_CLIENT_QBITTORRENT:
                    // qBittorrent Web API
                    apiUrl = baseUrl + "/api/v2/torrents/add";
                    sendToQBittorrent(apiUrl, magnetUrl, callback);
                    break;
                    
                case ServerProfile.TORRENT_CLIENT_TRANSMISSION:
                    // Transmission RPC API
                    apiUrl = baseUrl + "/transmission/rpc";
                    sendToTransmission(apiUrl, magnetUrl, callback);
                    break;
                    
                case ServerProfile.TORRENT_CLIENTUTORRENT:
                    // uTorrent Web API
                    apiUrl = baseUrl + "/gui/";
                    sendToUTorrent(apiUrl, magnetUrl, callback);
                    break;
                    
                default:
                    callback.onError("Unsupported torrent client: " + profile.getTorrentClientType());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending magnet to torrent client", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send to qBittorrent
     */
    private void sendToQBittorrent(String apiUrl, String url, ServiceApiCallback callback) {
        try {
            // qBittorrent expects form data
            RequestBody formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("urls", url)
                    .build();
            
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(formBody)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send to qBittorrent", e);
                    callback.onError(e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "qBittorrent API error: " + errorBody);
                            callback.onError("API Error: " + response.code() + " - " + errorBody);
                        }
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing qBittorrent request", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send to Transmission
     */
    private void sendToTransmission(String apiUrl, String url, ServiceApiCallback callback) {
        try {
            // Transmission expects JSON-RPC
            JSONObject json = new JSONObject();
            json.put("method", "torrent-add");
            JSONObject params = new JSONObject();
            
            // Transmission can handle both URLs and magnet links
            if (url.startsWith("magnet:")) {
                params.put("filename", url);
            } else {
                params.put("filename", url);
            }
            
            json.put("arguments", params);
            
            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("X-Transmission-Session-Id", "dummy") // May need to handle session ID properly
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send to Transmission", e);
                    callback.onError(e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "Transmission API error: " + errorBody);
                            callback.onError("API Error: " + response.code() + " - " + errorBody);
                        }
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing Transmission request", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send to uTorrent
     */
    private void sendToUTorrent(String apiUrl, String url, ServiceApiCallback callback) {
        try {
            // uTorrent Web API - this is a simplified version
            String fullUrl = apiUrl + "?action=add-url&s=" + url;
            
            Request request = new Request.Builder()
                    .url(fullUrl)
                    .get()
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send to uTorrent", e);
                    callback.onError(e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "uTorrent API error: " + errorBody);
                            callback.onError("API Error: " + response.code() + " - " + errorBody);
                        }
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing uTorrent request", e);
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * Send URL to jDownloader
     */
    private void sendUrlToJDownloader(ServerProfile profile, String url, ServiceApiCallback callback) {
        try {
            // jDownloader My.JDownloader API or direct API
            String baseUrl = profile.getUrl() + ":" + profile.getPort();
            String apiUrl = baseUrl + "/flashget"; // Common jDownloader endpoint for direct downloads
            
            // jDownloader typically accepts GET requests with URL parameters
            String fullUrl = apiUrl + "?url=" + url;
            
            Request request = new Request.Builder()
                    .url(fullUrl)
                    .get()
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send to jDownloader", e);
                    callback.onError(e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "jDownloader API error: " + errorBody);
                            callback.onError("API Error: " + response.code() + " - " + errorBody);
                        }
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing jDownloader request", e);
            callback.onError(e.getMessage());
        }
    }
}