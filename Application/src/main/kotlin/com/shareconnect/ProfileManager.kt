package com.shareconnect

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ProfileManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val gson: Gson

    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gson = Gson()
    }

    val profiles: List<ServerProfile>
        get() {
            val profilesJson = sharedPreferences.getString(KEY_PROFILES, null)
            if (profilesJson != null) {
                val listType = object : TypeToken<List<ServerProfile>>() {}.type
                return gson.fromJson(profilesJson, listType)
            }
            return ArrayList()
        }

    fun saveProfiles(profiles: List<ServerProfile>) {
        val profilesJson = gson.toJson(profiles)
        sharedPreferences.edit().putString(KEY_PROFILES, profilesJson).apply()
    }

    fun defaultProfile(): ServerProfile? {
        val defaultProfileId = sharedPreferences.getString(KEY_DEFAULT_PROFILE, null)
        if (defaultProfileId != null) {
            val profiles = profiles
            for (profile in profiles) {
                if (profile.id == defaultProfileId) {
                    return profile
                }
            }
        }
        // If no default profile is set, return the first profile if available
        val profiles = profiles
        return if (profiles.isNotEmpty()) {
            profiles[0]
        } else null
    }

    fun setDefaultProfile(profile: ServerProfile) {
        sharedPreferences.edit().putString(KEY_DEFAULT_PROFILE, profile.id).apply()
    }

    fun addProfile(profile: ServerProfile) {
        if (profile.id.isNullOrEmpty()) {
            profile.id = UUID.randomUUID().toString()
        }

        // Set default service type if not set
        if (profile.serviceType.isNullOrEmpty()) {
            profile.serviceType = ServerProfile.TYPE_METUBE
        }

        val profiles = profiles.toMutableList()
        profiles.add(profile)
        saveProfiles(profiles)
    }

    fun updateProfile(profile: ServerProfile) {
        val profiles = profiles.toMutableList()
        for (i in profiles.indices) {
            if (profiles[i].id == profile.id) {
                profiles[i] = profile
                break
            }
        }
        saveProfiles(profiles)
    }

    fun deleteProfile(profile: ServerProfile) {
        val profiles = profiles.toMutableList()
        profiles.remove(profile)
        saveProfiles(profiles)

        // If we're deleting the default profile, clear the default setting
        val defaultProfileId = sharedPreferences.getString(KEY_DEFAULT_PROFILE, null)
        if (defaultProfileId != null && defaultProfileId == profile.id) {
            sharedPreferences.edit().remove(KEY_DEFAULT_PROFILE).apply()
        }
    }

    fun hasProfiles(): Boolean {
        return profiles.isNotEmpty()
    }

    /**
     * Get profiles filtered by service type
     */
    fun getProfilesByServiceType(serviceType: String): List<ServerProfile> {
        val allProfiles = profiles
        val filteredProfiles: MutableList<ServerProfile> = ArrayList()

        for (profile in allProfiles) {
            if (serviceType == profile.serviceType) {
                filteredProfiles.add(profile)
            }
        }

        return filteredProfiles
    }

    /**
     * Get all unique service types from existing profiles
     */
    fun allServiceTypes(): List<String> {
        val allProfiles = profiles
        val serviceTypes: MutableList<String> = ArrayList()

        for (profile in allProfiles) {
            val serviceType = profile.serviceType
            if (serviceType != null && !serviceTypes.contains(serviceType)) {
                serviceTypes.add(serviceType)
            }
        }

        return serviceTypes
    }

    /**
     * Get all torrent client types from existing profiles
     */
    fun allTorrentClientTypes(): List<String> {
        val allProfiles = profiles
        val clientTypes: MutableList<String> = ArrayList()

        for (profile in allProfiles) {
            if (ServerProfile.TYPE_TORRENT == profile.serviceType) {
                val clientType = profile.torrentClientType
                if (clientType != null && !clientTypes.contains(clientType)) {
                    clientTypes.add(clientType)
                }
            }
        }

        return clientTypes
    }

    companion object {
        private const val PREFS_NAME = "MeTubeSharePrefs"
        private const val KEY_PROFILES = "profiles"
        private const val KEY_DEFAULT_PROFILE = "default_profile"
    }
}