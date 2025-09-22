package com.metubeshare;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileManager {
    private static final String PREFS_NAME = "MeTubeSharePrefs";
    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_DEFAULT_PROFILE = "default_profile";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public ProfileManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<ServerProfile> getProfiles() {
        String profilesJson = sharedPreferences.getString(KEY_PROFILES, null);
        if (profilesJson != null) {
            Type listType = new TypeToken<List<ServerProfile>>(){}.getType();
            return gson.fromJson(profilesJson, listType);
        }
        return new ArrayList<>();
    }

    public void saveProfiles(List<ServerProfile> profiles) {
        String profilesJson = gson.toJson(profiles);
        sharedPreferences.edit().putString(KEY_PROFILES, profilesJson).apply();
    }

    public ServerProfile getDefaultProfile() {
        String defaultProfileId = sharedPreferences.getString(KEY_DEFAULT_PROFILE, null);
        if (defaultProfileId != null) {
            List<ServerProfile> profiles = getProfiles();
            for (ServerProfile profile : profiles) {
                if (profile.getId().equals(defaultProfileId)) {
                    return profile;
                }
            }
        }
        // If no default profile is set, return the first profile if available
        List<ServerProfile> profiles = getProfiles();
        if (!profiles.isEmpty()) {
            return profiles.get(0);
        }
        return null;
    }

    public void setDefaultProfile(ServerProfile profile) {
        sharedPreferences.edit().putString(KEY_DEFAULT_PROFILE, profile.getId()).apply();
    }

    public void addProfile(ServerProfile profile) {
        if (profile.getId() == null || profile.getId().isEmpty()) {
            profile.setId(UUID.randomUUID().toString());
        }
        
        List<ServerProfile> profiles = getProfiles();
        profiles.add(profile);
        saveProfiles(profiles);
    }

    public void updateProfile(ServerProfile profile) {
        List<ServerProfile> profiles = getProfiles();
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getId().equals(profile.getId())) {
                profiles.set(i, profile);
                break;
            }
        }
        saveProfiles(profiles);
    }

    public void deleteProfile(ServerProfile profile) {
        List<ServerProfile> profiles = getProfiles();
        profiles.remove(profile);
        saveProfiles(profiles);
        
        // If we're deleting the default profile, clear the default setting
        String defaultProfileId = sharedPreferences.getString(KEY_DEFAULT_PROFILE, null);
        if (defaultProfileId != null && defaultProfileId.equals(profile.getId())) {
            sharedPreferences.edit().remove(KEY_DEFAULT_PROFILE).apply();
        }
    }

    public boolean hasProfiles() {
        return !getProfiles().isEmpty();
    }
}