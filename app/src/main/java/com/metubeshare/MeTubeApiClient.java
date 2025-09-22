package com.metubeshare;

import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class MeTubeApiClient {
    private static final String TAG = "MeTubeApiClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    
    public MeTubeApiClient() {
        client = new OkHttpClient();
    }
    
    public interface MeTubeApiCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public void sendUrlToMeTube(ServerProfile profile, String youtubeUrl, MeTubeApiCallback callback) {
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
}