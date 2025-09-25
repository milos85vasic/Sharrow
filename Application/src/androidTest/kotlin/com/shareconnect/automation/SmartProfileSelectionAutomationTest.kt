package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.R
import com.shareconnect.ServerProfile
import com.shareconnect.ShareActivity
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmartProfileSelectionAutomationTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private lateinit var profileManager: ProfileManager
    private var testProfiles: MutableList<ServerProfile> = mutableListOf()

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        profileManager = ProfileManager(context)

        // Clear any existing profiles first
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        // Ensure screen is on and unlocked
        device.wakeUp()
        device.pressHome()
    }

    @After
    fun tearDown() {
        // Clean up test profiles
        for (profile in testProfiles) {
            profileManager.deleteProfile(profile)
        }
        testProfiles.clear()

        device.pressHome()
    }

    @Test
    fun testIncompatibleDefaultProfileNotSelected() {
        // Create a MeTube profile and set it as default (incompatible with torrent URLs)
        val metubeProfile = createProfile("MeTube Default", ServerProfile.TYPE_METUBE)
        profileManager.setDefaultProfile(metubeProfile)

        // Create a qBittorrent profile (compatible with torrent URLs)
        val qbitProfile = createProfile("qBittorrent Server", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with a magnet link (should NOT select MeTube default)
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "magnet:?xt=urn:btih:test123&dn=TestTorrent")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify that the qBittorrent profile is selected instead of the default MeTube
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("qBittorrent Server"))))
            .check(matches(not(withText(containsString("MeTube Default")))))

        scenario.close()
    }

    @Test
    fun testCompatibleDefaultProfileIsSelected() {
        // Create a qBittorrent profile and set it as default (compatible with torrent URLs)
        val qbitProfile = createProfile("qBittorrent Default", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        profileManager.setDefaultProfile(qbitProfile)

        // Create another qBittorrent profile
        val anotherQbitProfile = createProfile("Another qBittorrent", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with a magnet link (should select the default qBittorrent)
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "magnet:?xt=urn:btih:test123&dn=TestTorrent")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify that the default compatible profile is selected
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("qBittorrent Default"))))

        scenario.close()
    }

    @Test
    fun testFirstCompatibleProfileSelectedWhenNoDefault() {
        // Create multiple profiles of different types (no default set)
        val metubeProfile = createProfile("MeTube Server", ServerProfile.TYPE_METUBE) // Incompatible with torrents
        val ytdlProfile = createProfile("YT-DLP Server", ServerProfile.TYPE_YTDL) // Incompatible with torrents
        val qbitProfile = createProfile("qBittorrent Server", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT) // Compatible!
        val transmissionProfile = createProfile("Transmission Server", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_TRANSMISSION) // Also compatible

        // Ensure no default is set
        profileManager.clearDefaultProfile()

        // Start ShareActivity with a magnet link
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "magnet:?xt=urn:btih:test123&dn=TestTorrent")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Should select the first compatible profile (qBittorrent, since it was created first)
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("qBittorrent Server"))))

        scenario.close()
    }

    @Test
    fun testStreamingUrlSelectsStreamingProfile() {
        // Create profiles: torrent (incompatible) and MeTube (compatible)
        val qbitProfile = createProfile("qBittorrent Server", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        profileManager.setDefaultProfile(qbitProfile) // Set torrent as default (incompatible with streaming)

        val metubeProfile = createProfile("MeTube Server", ServerProfile.TYPE_METUBE) // Compatible with streaming

        // Start ShareActivity with YouTube URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Should select MeTube (compatible) instead of qBittorrent (default but incompatible)
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("MeTube Server"))))
            .check(matches(not(withText(containsString("qBittorrent Server")))))

        scenario.close()
    }

    @Test
    fun testDirectDownloadUrlSelectsDownloadCapableProfile() {
        // Create profiles: MeTube (incompatible) and YT-DLP (compatible)
        val metubeProfile = createProfile("MeTube Server", ServerProfile.TYPE_METUBE) // Cannot handle direct downloads
        profileManager.setDefaultProfile(metubeProfile) // Set as default

        val ytdlProfile = createProfile("YT-DLP Server", ServerProfile.TYPE_YTDL) // Can handle direct downloads

        // Start ShareActivity with direct download URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/file.zip")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Should select YT-DLP (compatible) instead of MeTube (default but incompatible)
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("YT-DLP Server"))))
            .check(matches(not(withText(containsString("MeTube Server")))))

        scenario.close()
    }

    @Test
    fun testMultipleCompatibleProfilesSelectsFirst() {
        // Create multiple streaming-compatible profiles
        val profile1 = createProfile("MeTube Server A", ServerProfile.TYPE_METUBE)
        val profile2 = createProfile("YT-DLP Server", ServerProfile.TYPE_YTDL)
        val profile3 = createProfile("MeTube Server B", ServerProfile.TYPE_METUBE)
        val profile4 = createProfile("jDownloader Server", ServerProfile.TYPE_JDOWNLOADER)

        // Set incompatible profile as default
        val torrentProfile = createProfile("qBittorrent Server", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        profileManager.setDefaultProfile(torrentProfile)

        // Start ShareActivity with streaming URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Should select the first compatible profile created (MeTube Server A)
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("MeTube Server A"))))

        scenario.close()
    }

    @Test
    fun testPriorityOrderWithCompatibleDefault() {
        // Create profiles in specific order
        val profile1 = createProfile("First MeTube", ServerProfile.TYPE_METUBE)
        val profile2 = createProfile("Second MeTube", ServerProfile.TYPE_METUBE)

        // Set the second one as default
        profileManager.setDefaultProfile(profile2)

        // Start ShareActivity with streaming URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Should prioritize the default profile even though it's not first
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("Second MeTube"))))

        scenario.close()
    }

    @Test
    fun testInvalidUrlHandling() {
        // Create mixed profiles
        val metubeProfile = createProfile("MeTube Server", ServerProfile.TYPE_METUBE)
        profileManager.setDefaultProfile(metubeProfile)
        val qbitProfile = createProfile("qBittorrent Server", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with invalid URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "invalid-url-format")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // For invalid URLs, the system should fall back to showing all profiles
        // and select the default one since URL type cannot be determined
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("MeTube Server"))))

        scenario.close()
    }

    @Test
    fun testEmptyUrlHandling() {
        // Create profiles
        val metubeProfile = createProfile("MeTube Server", ServerProfile.TYPE_METUBE)
        profileManager.setDefaultProfile(metubeProfile)

        // Start ShareActivity with empty URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // For empty URLs, should show all profiles and select default
        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(withText(containsString("MeTube Server"))))

        scenario.close()
    }

    private fun createProfile(
        name: String,
        serviceType: String,
        torrentClientType: String? = null
    ): ServerProfile {
        val profile = ServerProfile()
        profile.id = "test-$name-${System.currentTimeMillis()}-${Math.random()}"
        profile.name = name
        profile.url = "http://localhost"
        profile.port = 8080
        profile.serviceType = serviceType
        profile.torrentClientType = torrentClientType
        profile.username = "testuser"
        profile.password = "testpass"

        profileManager.addProfile(profile)
        testProfiles.add(profile)
        return profile
    }
}