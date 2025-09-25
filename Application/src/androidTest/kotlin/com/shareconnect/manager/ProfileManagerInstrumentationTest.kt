package com.shareconnect.manager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shareconnect.ProfileManager
import com.shareconnect.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileManagerInstrumentationTest {

    private lateinit var context: Context
    private lateinit var profileManager: ProfileManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        profileManager = ProfileManager(context)
    }

    @Test
    fun testProfileManagerInitialization() {
        assertNotNull(profileManager)
    }

    @Test
    fun testHasProfilesWithEmptyProfiles() {
        // Clear any existing profiles
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        assertFalse(profileManager.hasProfiles())
    }

    @Test
    fun testHasProfilesWithExistingProfiles() {
        // Clear any existing profiles first
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        // Add a test profile
        val testProfile = ServerProfile()
        testProfile.id = "test-id"
        testProfile.name = "Test Profile"
        testProfile.url = "http://example.com"
        testProfile.port = 8080
        testProfile.serviceType = ServerProfile.TYPE_METUBE

        profileManager.addProfile(testProfile)

        assertTrue(profileManager.hasProfiles())
    }

    @Test
    fun testGetProfilesEmpty() {
        // Clear any existing profiles
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        val profiles = profileManager.profiles

        assertTrue(profiles.isEmpty())
    }

    @Test
    fun testGetProfiles() {
        // Clear any existing profiles first
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        // Add a test profile
        val testProfile = ServerProfile()
        testProfile.id = "test-id"
        testProfile.name = "Test Profile"
        testProfile.url = "http://example.com"
        testProfile.port = 8080
        testProfile.serviceType = ServerProfile.TYPE_METUBE
        testProfile.username = null
        testProfile.password = null

        profileManager.addProfile(testProfile)

        val profiles = profileManager.profiles

        assertEquals(1, profiles.size)
        assertEquals("test-id", profiles[0].id)
        assertEquals("Test Profile", profiles[0].name)
        assertEquals(null, profiles[0].username)
        assertEquals(null, profiles[0].password)
    }

    @Test
    fun testDefaultProfile() {
        // Clear any existing profiles
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        val profile = profileManager.defaultProfile()

        assertTrue(profile == null)
    }

    @Test
    fun testGetProfilesByServiceType() {
        // Clear any existing profiles first
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        // Add test profiles
        val metubeProfile = ServerProfile()
        metubeProfile.id = "test-id-1"
        metubeProfile.name = "Profile 1"
        metubeProfile.url = "http://example1.com"
        metubeProfile.port = 8080
        metubeProfile.serviceType = ServerProfile.TYPE_METUBE
        metubeProfile.username = "user1"
        metubeProfile.password = "pass1"

        val ytdlProfile = ServerProfile()
        ytdlProfile.id = "test-id-2"
        ytdlProfile.name = "Profile 2"
        ytdlProfile.url = "http://example2.com"
        ytdlProfile.port = 9090
        ytdlProfile.serviceType = ServerProfile.TYPE_YTDL
        ytdlProfile.username = null
        ytdlProfile.password = null

        profileManager.addProfile(metubeProfile)
        profileManager.addProfile(ytdlProfile)

        val metubeProfiles = profileManager.getProfilesByServiceType(ServerProfile.TYPE_METUBE)

        assertEquals(1, metubeProfiles.size)
        assertEquals("Profile 1", metubeProfiles[0].name)
        assertEquals("user1", metubeProfiles[0].username)
        assertEquals("pass1", metubeProfiles[0].password)

        val ytdlProfiles = profileManager.getProfilesByServiceType(ServerProfile.TYPE_YTDL)
        assertEquals(1, ytdlProfiles.size)
        assertEquals("Profile 2", ytdlProfiles[0].name)
        assertEquals(null, ytdlProfiles[0].username)
        assertEquals(null, ytdlProfiles[0].password)
    }
}