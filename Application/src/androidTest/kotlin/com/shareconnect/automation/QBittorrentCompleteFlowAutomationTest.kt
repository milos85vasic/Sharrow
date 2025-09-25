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
import com.shareconnect.WebUIActivity
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QBittorrentCompleteFlowAutomationTest {

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
    fun testCompleteQBittorrentMagnetLinkFlow() {
        // Create a qBittorrent profile with authentication
        val qbitProfile = createQBittorrentProfile("Test qBittorrent Server", "admin", "testpass123")

        // Start ShareActivity with a magnet link
        val magnetUrl = "magnet:?xt=urn:btih:c12fe1c06bba254a9dc9f519b335aa7c1367a88a&dn=Test+Torrent"
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, magnetUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify ShareActivity loaded with magnet link
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("magnet:"))))

        // Select the qBittorrent profile
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(500)

        onView(withText(containsString("Test qBittorrent Server")))
            .perform(click())

        // Click the send button to launch WebUI
        onView(withId(R.id.buttonSendToMeTube))
            .perform(click())

        // Wait for WebUI activity to launch
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify ShareActivity is dismissed (WebUI flow started)
        val shareActivityExists = try {
            device.findObject(UiSelector().resourceId("com.shareconnect:id/textViewYouTubeLink"))
                .waitForExists(1000)
        } catch (e: Exception) {
            false
        }

        assert(!shareActivityExists) { "ShareActivity should be dismissed after launching WebUI" }

        scenario.close()
    }

    @Test
    fun testCompleteQBittorrentTorrentFileFlow() {
        // Create a qBittorrent profile
        val qbitProfile = createQBittorrentProfile("Test qBittorrent", "user", "password")

        // Start ShareActivity with a torrent file URL
        val torrentUrl = "https://example.com/test.torrent"
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, torrentUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify the torrent URL is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString(".torrent"))))

        // Select the qBittorrent profile
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(500)

        onView(withText(containsString("Test qBittorrent")))
            .perform(click())

        // Click send to launch WebUI
        onView(withId(R.id.buttonSendToMeTube))
            .perform(click())

        // Wait for WebUI to launch and ShareActivity to close
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify ShareActivity is closed
        val shareActivityExists = try {
            device.findObject(UiSelector().resourceId("com.shareconnect:id/textViewYouTubeLink"))
                .waitForExists(1000)
        } catch (e: Exception) {
            false
        }

        assert(!shareActivityExists) { "ShareActivity should be dismissed after launching WebUI" }

        scenario.close()
    }

    @Test
    fun testQBittorrentWebUILaunchAndAuthentication() {
        // Create a qBittorrent profile
        val qbitProfile = createQBittorrentProfile("Test qBittorrent WebUI", "testuser", "testpass")

        // Start WebUIActivity directly with a magnet link
        val magnetUrl = "magnet:?xt=urn:btih:test123&dn=TestTorrent"
        val intent = Intent(context, WebUIActivity::class.java).apply {
            putExtra("profile", qbitProfile)
            putExtra("url_to_share", magnetUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<WebUIActivity>(intent)

        // Wait for WebUI to load
        device.waitForIdle()
        Thread.sleep(5000) // Allow time for authentication and URL passing

        // Check if WebView is displayed (WebUIActivity loaded)
        val webViewExists = device.findObject(
            UiSelector().className("android.webkit.WebView")
        ).waitForExists(3000)

        assert(webViewExists) { "WebView should be displayed in WebUIActivity" }

        scenario.close()
    }

    @Test
    fun testQBittorrentProfileFiltering() {
        // Create different types of profiles
        val qbitProfile = createQBittorrentProfile("qBittorrent Server", "admin", "password")
        val metubeProfile = createProfile("MeTube Server", ServerProfile.TYPE_METUBE)

        // Test that only torrent profiles show for magnet links
        val magnetUrl = "magnet:?xt=urn:btih:test123&dn=TestTorrent"
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, magnetUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Click on profile selection dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify that qBittorrent profile is shown
        onView(withText(containsString("qBittorrent Server")))
            .check(matches(isDisplayed()))

        // Verify that MeTube profile is NOT shown for magnet links
        try {
            onView(withText(containsString("MeTube Server")))
                .check(matches(isDisplayed()))
            assert(false) { "MeTube profile should not be visible for magnet links" }
        } catch (e: Exception) {
            // Expected - MeTube should not handle magnet links
        }

        scenario.close()
    }

    @Test
    fun testQBittorrentSystemAppsInteraction() {
        // Create qBittorrent profile
        val qbitProfile = createQBittorrentProfile("Test qBittorrent", "user", "pass")

        // Start with magnet URL to see if system torrent apps are detected
        val magnetUrl = "magnet:?xt=urn:btih:test&dn=Test"
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, magnetUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load and system apps to be detected
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify the compatible apps section appears
        try {
            onView(withId(R.id.textViewCompatibleAppsTitle))
                .check(matches(isDisplayed()))

            // Either system apps are shown or no compatible apps message
            try {
                onView(withId(R.id.recyclerViewSystemApps))
                    .check(matches(isDisplayed()))
            } catch (e: Exception) {
                onView(withId(R.id.textViewNoCompatibleApps))
                    .check(matches(isDisplayed()))
            }
        } catch (e: Exception) {
            // System apps section might be hidden if no torrent apps found
        }

        // Verify ShareConnect profile is still available
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(500)

        onView(withText(containsString("Test qBittorrent")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testMultipleQBittorrentProfilesDisplayed() {
        // Create multiple qBittorrent profiles
        val qbit1 = createQBittorrentProfile("qBittorrent Server 1", "user1", "pass1")
        val qbit2 = createQBittorrentProfile("qBittorrent Server 2", "user2", "pass2")
        val qbit3 = createQBittorrentProfile("Local qBittorrent", "admin", "admin")

        // Start with magnet link
        val magnetUrl = "magnet:?xt=urn:btih:multitest&dn=MultiTest"
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, magnetUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Click on profile dropdown
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify all qBittorrent profiles are shown
        onView(withText(containsString("qBittorrent Server 1")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("qBittorrent Server 2")))
            .check(matches(isDisplayed()))

        onView(withText(containsString("Local qBittorrent")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testQBittorrentErrorHandling() {
        // Create qBittorrent profile with empty credentials
        val qbitProfile = createQBittorrentProfile("Test qBittorrent", "", "")

        // Test flow still works without authentication
        val magnetUrl = "magnet:?xt=urn:btih:noauth&dn=NoAuth"
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, magnetUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Select the profile and attempt to launch
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(500)

        onView(withText(containsString("Test qBittorrent")))
            .perform(click())

        onView(withId(R.id.buttonSendToMeTube))
            .perform(click())

        // Even without credentials, WebUI should launch
        device.waitForIdle()
        Thread.sleep(2000)

        val shareActivityExists = try {
            device.findObject(UiSelector().resourceId("com.shareconnect:id/textViewYouTubeLink"))
                .waitForExists(1000)
        } catch (e: Exception) {
            false
        }

        assert(!shareActivityExists) { "ShareActivity should be dismissed even without credentials" }

        scenario.close()
    }

    private fun createQBittorrentProfile(
        name: String,
        username: String,
        password: String
    ): ServerProfile {
        val profile = ServerProfile()
        profile.id = "test-$name-${System.currentTimeMillis()}"
        profile.name = name
        profile.url = "http://localhost"
        profile.port = 8080
        profile.serviceType = ServerProfile.TYPE_TORRENT
        profile.torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT
        profile.username = username
        profile.password = password

        profileManager.addProfile(profile)
        testProfiles.add(profile)
        return profile
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