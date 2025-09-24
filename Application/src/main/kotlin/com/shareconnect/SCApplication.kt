package com.shareconnect

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.redelf.commons.application.BaseApplication
import com.shareconnect.database.HistoryDatabase

class SCApplication : BaseApplication() {

    lateinit var database: HistoryDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        initializeDatabase()
        migrateProfilesToDatabase()
    }

    private fun initializeDatabase() {
        // Initialize database without encryption for now
        // SQLCipher will be configured separately through the toolkit
        database = Room.databaseBuilder(
            applicationContext,
            HistoryDatabase::class.java,
            "history_database"
        )
            .addMigrations(MIGRATION_2_3)
            .allowMainThreadQueries() // For simplicity
            .fallbackToDestructiveMigration() // For development
            .build()
    }

    private fun migrateProfilesToDatabase() {
        // Migrate existing profiles from SharedPreferences to Room database
        val sharedPrefs = getSharedPreferences("MeTubeSharePrefs", MODE_PRIVATE)
        val profilesJson = sharedPrefs.getString("profiles", null)

        if (profilesJson != null && !sharedPrefs.getBoolean("profiles_migrated", false)) {
            try {
                val profileManager = ProfileManager(this)
                // ProfileManager will handle the migration internally
                sharedPrefs.edit().putBoolean("profiles_migrated", true).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun firebaseEnabled() = isProduction()
    override fun firebaseAnalyticsEnabled() = isProduction()

    override fun isProduction(): Boolean {

        return resources.getBoolean(R.bool.is_production)
    }

    override fun takeSalt(): String {

        return getString(R.string.app_name)
    }

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create server_profiles table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS server_profiles (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT,
                        url TEXT,
                        port INTEGER NOT NULL DEFAULT 0,
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        serviceType TEXT,
                        torrentClientType TEXT,
                        username TEXT,
                        password TEXT
                    )
                """.trimIndent())
            }
        }
    }
}