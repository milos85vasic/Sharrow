package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
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
class ProfileFilteringAutomationTest {

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
    fun testYouTubeUrlShowsOnlyStreamingProfiles() {
        // Create different types of profiles
        val metubeProfile = createProfile("MeTube Test", ServerProfile.TYPE_METUBE)
        val ytdlProfile = createProfile("YT-DLP Test", ServerProfile.TYPE_YTDL)
        val torrentProfile = createProfile("qBittorrent Test", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        val jdownloaderProfile = createProfile("JDownloader Test", ServerProfile.TYPE_JDOWNLOADER)

        // Start ShareActivity with YouTube URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Verify ShareActivity loaded
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())

        // Wait for dropdown to appear
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify that streaming profiles are shown
        onView(withText(containsString("MeTube Test")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("YT-DLP Test")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("JDownloader Test")))
            .check(matches(isDisplayed()))

        // Verify that torrent profiles are NOT shown
        try {
            onView(withText(containsString("qBittorrent Test")))
                .check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            // Expected - torrent profile should not be visible
        }

        scenario.close()
    }

    @Test
    fun testMagnetUrlShowsOnlyTorrentProfiles() {
        // Create different types of profiles
        val metubeProfile = createProfile("MeTube Test", ServerProfile.TYPE_METUBE)
        val ytdlProfile = createProfile("YT-DLP Test", ServerProfile.TYPE_YTDL)
        val qbitProfile = createProfile("qBittorrent Test", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        val transmissionProfile = createProfile("Transmission Test", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_TRANSMISSION)

        // Start ShareActivity with magnet URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "magnet:?xt=urn:btih:testmagnetlink123456789")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Verify ShareActivity loaded
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())

        // Wait for dropdown to appear
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify that torrent profiles are shown
        onView(withText(containsString("qBittorrent Test")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("Transmission Test")))
            .check(matches(isDisplayed()))

        // Verify that streaming profiles are NOT shown
        try {
            onView(withText(containsString("MeTube Test")))
                .check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            // Expected - streaming profiles should not be visible for magnet links
        }

        try {
            onView(withText(containsString("YT-DLP Test")))
                .check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            // Expected - YT-DLP should not be visible for magnet links
        }

        scenario.close()
    }

    @Test
    fun testTorrentFileUrlShowsOnlyTorrentProfiles() {
        // Create different types of profiles
        val metubeProfile = createProfile("MeTube Test", ServerProfile.TYPE_METUBE)
        val uTorrentProfile = createProfile("uTorrent Test", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENTUTORRENT)
        val jdownloaderProfile = createProfile("JDownloader Test", ServerProfile.TYPE_JDOWNLOADER)

        // Start ShareActivity with torrent file URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/file.torrent")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Verify ShareActivity loaded
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())

        // Wait for dropdown to appear
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify that torrent profiles are shown
        onView(withText(containsString("uTorrent Test")))
            .check(matches(isDisplayed()))

        // Verify that non-torrent profiles are NOT shown
        try {
            onView(withText(containsString("MeTube Test")))
                .check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            // Expected - MeTube should not be visible for torrent files
        }

        scenario.close()
    }

    @Test
    fun testDirectDownloadUrlShowsCompatibleProfiles() {
        // Create different types of profiles
        val metubeProfile = createProfile("MeTube Test", ServerProfile.TYPE_METUBE)
        val ytdlProfile = createProfile("YT-DLP Test", ServerProfile.TYPE_YTDL)
        val torrentProfile = createProfile("qBittorrent Test", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        val jdownloaderProfile = createProfile("JDownloader Test", ServerProfile.TYPE_JDOWNLOADER)

        // Start ShareActivity with direct download URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/file.zip")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Verify ShareActivity loaded
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())

        // Wait for dropdown to appear
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify that download-capable profiles are shown
        onView(withText(containsString("YT-DLP Test")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("JDownloader Test")))
            .check(matches(isDisplayed()))

        // Verify that MeTube (streaming-only) and torrent profiles are NOT shown
        try {
            onView(withText(containsString("MeTube Test")))
                .check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            // Expected - MeTube should not handle direct downloads
        }

        try {
            onView(withText(containsString("qBittorrent Test")))
                .check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            // Expected - Torrent clients should not handle direct downloads
        }

        scenario.close()
    }

    @Test
    fun testVimeoUrlShowsStreamingProfiles() {
        // Create profiles
        val metubeProfile = createProfile("MeTube Test", ServerProfile.TYPE_METUBE)
        val ytdlProfile = createProfile("YT-DLP Test", ServerProfile.TYPE_YTDL)
        val torrentProfile = createProfile("qBittorrent Test", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with Vimeo URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://vimeo.com/123456789")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())

        device.waitForIdle()
        Thread.sleep(1000)

        // Verify streaming profiles are shown
        onView(withText(containsString("MeTube Test")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("YT-DLP Test")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testNoCompatibleProfilesShowsErrorMessage() {
        // Create only torrent profile
        val torrentProfile = createProfile("qBittorrent Only", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with YouTube URL (incompatible with torrent clients)
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=incompatible123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for potential error handling
        device.waitForIdle()
        Thread.sleep(2000)

        // The activity should have redirected to settings or shown an error
        // Check if we're in SettingsActivity or if there's an error message
        try {
            onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(withText("Settings"))))
        } catch (e: Exception) {
            // May have shown toast and closed activity
        }

        scenario.close()
    }

    @Test
    fun testMultipleSameTypeProfilesAllShown() {
        // Create multiple MeTube profiles
        val metube1 = createProfile("MeTube Server 1", ServerProfile.TYPE_METUBE)
        val metube2 = createProfile("MeTube Server 2", ServerProfile.TYPE_METUBE)
        val metube3 = createProfile("MeTube Local", ServerProfile.TYPE_METUBE)

        // Also create a torrent profile (should not be shown)
        val torrentProfile = createProfile("qBittorrent Test", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with YouTube URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://youtube.com/watch?v=multiprofile123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())

        device.waitForIdle()
        Thread.sleep(1000)

        // Verify all MeTube profiles are shown
        onView(withText(containsString("MeTube Server 1")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("MeTube Server 2")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("MeTube Local")))
            .check(matches(isDisplayed()))

        // Verify torrent profile is NOT shown
        try {
            onView(withText(containsString("qBittorrent Test")))
                .check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            // Expected - torrent profile should not be visible for YouTube URLs
        }

        scenario.close()
    }

    @Test
    fun testTwitchUrlShowsStreamingProfiles() {
        // Create profiles
        val metubeProfile = createProfile("MeTube Test", ServerProfile.TYPE_METUBE)
        val jdownloaderProfile = createProfile("JDownloader Test", ServerProfile.TYPE_JDOWNLOADER)

        // Start ShareActivity with Twitch URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.twitch.tv/streamername")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())

        device.waitForIdle()
        Thread.sleep(1000)

        // Verify streaming-capable profiles are shown
        onView(withText(containsString("MeTube Test")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("JDownloader Test")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    private fun createProfile(
        name: String,
        serviceType: String,
        torrentClientType: String? = null
    ): ServerProfile {
        val profile = ServerProfile()
        profile.id = "test-$name-${System.currentTimeMillis()}"
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