package com.shareconnect

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shareconnect.database.ServerProfileRepository
import java.util.UUID

class ProfileManager(private val context: Context) {
    private val repository: ServerProfileRepository
    private val sharedPreferences: SharedPreferences
    private val gson: Gson

    init {
        repository = ServerProfileRepository(context)
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gson = Gson()

        // Migrate from SharedPreferences to Room if needed
        migrateIfNeeded()
    }

    private fun migrateIfNeeded() {
        if (!sharedPreferences.getBoolean("migrated_to_room", false)) {
            val profilesJson = sharedPreferences.getString(KEY_PROFILES, null)
            if (profilesJson != null) {
                val listType = object : TypeToken<List<ServerProfile>>() {}.type
                val oldProfiles: List<ServerProfile> = gson.fromJson(profilesJson, listType)

                // Migrate each profile to Room database
                oldProfiles.forEach { profile ->
                    if (profile.id.isNullOrEmpty()) {
                        profile.id = UUID.randomUUID().toString()
                    }
                    repository.addProfile(profile)
                }

                // Mark as migrated
                sharedPreferences.edit()
                    .putBoolean("migrated_to_room", true)
                    .remove(KEY_PROFILES) // Clean up old data
                    .apply()
            } else {
                // No old profiles, just mark as migrated
                sharedPreferences.edit()
                    .putBoolean("migrated_to_room", true)
                    .apply()
            }
        }
    }

    val profiles: List<ServerProfile>
        get() = repository.getAllProfiles()

    fun saveProfiles(profiles: List<ServerProfile>) {
        // This method is kept for backward compatibility but now uses Room
        // Clear all existing profiles and add new ones
        // Note: In a production app, this would be more sophisticated
        profiles.forEach { profile ->
            if (repository.getProfileById(profile.id ?: "") != null) {
                repository.updateProfile(profile)
            } else {
                repository.addProfile(profile)
            }
        }
    }

    fun defaultProfile(): ServerProfile? {
        val defaultProfile = repository.getDefaultProfile()
        if (defaultProfile != null) {
            return defaultProfile
        }

        // Fallback: check SharedPreferences for backward compatibility
        val defaultProfileId = sharedPreferences.getString(KEY_DEFAULT_PROFILE, null)
        if (defaultProfileId != null) {
            val profile = repository.getProfileById(defaultProfileId)
            if (profile != null) {
                // Migrate the default setting to Room
                repository.setDefaultProfile(defaultProfileId)
                sharedPreferences.edit().remove(KEY_DEFAULT_PROFILE).apply()
                return profile
            }
        }

        // If no default profile is set, return the first profile if available
        val profiles = profiles
        return if (profiles.isNotEmpty()) {
            profiles[0]
        } else null
    }

    fun setDefaultProfile(profile: ServerProfile) {
        profile.id?.let { repository.setDefaultProfile(it) }
    }

    fun addProfile(profile: ServerProfile) {
        if (profile.id.isNullOrEmpty()) {
            profile.id = UUID.randomUUID().toString()
        }

        // Set default service type if not set
        if (profile.serviceType.isNullOrEmpty()) {
            profile.serviceType = ServerProfile.TYPE_METUBE
        }

        repository.addProfile(profile)
    }

    fun updateProfile(profile: ServerProfile) {
        repository.updateProfile(profile)
    }

    fun deleteProfile(profile: ServerProfile) {
        repository.deleteProfile(profile)
    }

    fun hasProfiles(): Boolean {
        return repository.hasProfiles()
    }

    /**
     * Get profiles filtered by service type
     */
    fun getProfilesByServiceType(serviceType: String): List<ServerProfile> {
        return repository.getProfilesByServiceType(serviceType)
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