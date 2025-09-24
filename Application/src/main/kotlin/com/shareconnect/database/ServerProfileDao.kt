package com.shareconnect.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ServerProfileDao {
    @Query("SELECT * FROM server_profiles")
    fun getAllProfiles(): List<ServerProfileEntity>

    @Query("SELECT * FROM server_profiles WHERE id = :id")
    fun getProfileById(id: String): ServerProfileEntity?

    @Query("SELECT * FROM server_profiles WHERE isDefault = 1 LIMIT 1")
    fun getDefaultProfile(): ServerProfileEntity?

    @Query("SELECT * FROM server_profiles WHERE serviceType = :serviceType")
    fun getProfilesByServiceType(serviceType: String): List<ServerProfileEntity>

    @Insert
    fun insert(profile: ServerProfileEntity)

    @Update
    fun update(profile: ServerProfileEntity)

    @Delete
    fun delete(profile: ServerProfileEntity)

    @Query("DELETE FROM server_profiles")
    fun deleteAll()

    @Query("UPDATE server_profiles SET isDefault = 0")
    fun clearAllDefaults()

    @Query("UPDATE server_profiles SET isDefault = 1 WHERE id = :profileId")
    fun setDefaultProfile(profileId: String)
}