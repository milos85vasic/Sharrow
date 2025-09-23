package com.shareconnect.manager

import android.content.Context
import android.content.SharedPreferences
import com.shareconnect.ProfileManager
import com.shareconnect.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProfileManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var profileManager: ProfileManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        `when`(mockEditor.apply()).then { }

        profileManager = ProfileManager(mockContext)
    }

    @Test
    fun testProfileManagerInitialization() {
        assertNotNull(profileManager)
        verify(mockContext).getSharedPreferences("MeTubeSharePrefs", Context.MODE_PRIVATE)
    }

    @Test
    fun testHasProfilesWithEmptyProfiles() {
        `when`(mockSharedPreferences.getString("profiles", null)).thenReturn(null)

        assertFalse(profileManager.hasProfiles())
    }

    @Test
    fun testHasProfilesWithExistingProfiles() {
        val profilesJson = """[{"id":"test-id","name":"Test Profile","url":"http://example.com","port":8080,"serviceType":"metube"}]"""
        `when`(mockSharedPreferences.getString("profiles", null)).thenReturn(profilesJson)

        assertTrue(profileManager.hasProfiles())
    }

    @Test
    fun testGetProfilesEmpty() {
        `when`(mockSharedPreferences.getString("profiles", null)).thenReturn(null)

        val profiles = profileManager.profiles

        assertTrue(profiles.isEmpty())
    }

    @Test
    fun testGetProfiles() {
        val profilesJson = """[{"id":"test-id","name":"Test Profile","url":"http://example.com","port":8080,"serviceType":"metube"}]"""
        `when`(mockSharedPreferences.getString("profiles", null)).thenReturn(profilesJson)

        val profiles = profileManager.profiles

        assertEquals(1, profiles.size)
        assertEquals("test-id", profiles[0].id)
        assertEquals("Test Profile", profiles[0].name)
    }

    @Test
    fun testDefaultProfile() {
        `when`(mockSharedPreferences.getString("profiles", null)).thenReturn(null)
        `when`(mockSharedPreferences.getString("default_profile", null)).thenReturn(null)

        val profile = profileManager.defaultProfile()

        assertTrue(profile == null)
    }

    @Test
    fun testGetProfilesByServiceType() {
        val profilesJson = """[{"id":"test-id-1","name":"Profile 1","url":"http://example1.com","port":8080,"serviceType":"metube"},{"id":"test-id-2","name":"Profile 2","url":"http://example2.com","port":9090,"serviceType":"ytdl"}]"""
        `when`(mockSharedPreferences.getString("profiles", null)).thenReturn(profilesJson)

        val metubeProfiles = profileManager.getProfilesByServiceType(ServerProfile.TYPE_METUBE)

        assertEquals(1, metubeProfiles.size)
        assertEquals("Profile 1", metubeProfiles[0].name)
    }
}