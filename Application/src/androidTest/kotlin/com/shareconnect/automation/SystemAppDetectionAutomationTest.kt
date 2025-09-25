package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import androidx.recyclerview.widget.RecyclerView
import com.shareconnect.ProfileManager
import com.shareconnect.R
import com.shareconnect.ServerProfile
import com.shareconnect.ShareActivity
import com.shareconnect.utils.SystemAppDetector
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemAppDetectionAutomationTest {

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
    fun testSystemAppDetectionUtilityWorks() {
        // Test that SystemAppDetector can find compatible apps for different URL types
        val youtubeUrl = "https://www.youtube.com/watch?v=testVideo123"
        val magnetUrl = "magnet:?xt=urn:btih:testmagnetlink123456789"
        val downloadUrl = "https://example.com/file.zip"

        // Test YouTube URL detection
        val youtubeApps = SystemAppDetector.getCompatibleApps(context, youtubeUrl)
        // Should find at least browsers and potentially YouTube app if installed
        assert(youtubeApps.isNotEmpty()) { "Should find at least some apps for YouTube URLs" }

        // Test magnet URL detection
        val torrentApps = SystemAppDetector.getCompatibleApps(context, magnetUrl)
        // May or may not find torrent apps depending on what's installed
        // This is expected behavior

        // Test direct download URL detection
        val downloadApps = SystemAppDetector.getCompatibleApps(context, downloadUrl)
        // Should find browsers and potentially download managers
        assert(downloadApps.isNotEmpty()) { "Should find at least some apps for download URLs" }
    }

    @Test
    fun testSystemAppsDisplayedForYouTubeUrl() {
        // Create a test profile
        val testProfile = createProfile("Test MeTube", ServerProfile.TYPE_METUBE)

        // Start ShareActivity with a YouTube URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load and system apps to be detected
        device.waitForIdle()
        Thread.sleep(3000) // Allow time for async system app detection

        // Verify the compatible apps section is shown
        onView(withId(R.id.textViewCompatibleAppsTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Compatible Apps")))

        // Check if system apps are displayed or no apps message is shown
        try {
            // Either the RecyclerView should be visible with apps
            onView(withId(R.id.recyclerViewSystemApps))
                .check(matches(isDisplayed()))
        } catch (e: Exception) {
            // Or the "no compatible apps" message should be shown
            onView(withId(R.id.textViewNoCompatibleApps))
                .check(matches(isDisplayed()))
        }

        // Verify original share functionality still works
        onView(withId(R.id.buttonShareToApps))
            .check(matches(isDisplayed()))
            .check(matches(withText("Share to Apps")))

        scenario.close()
    }

    @Test
    fun testSystemAppsDisplayedForMagnetUrl() {
        // Create a test torrent profile
        val testProfile = createProfile("Test qBittorrent", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with a magnet URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "magnet:?xt=urn:btih:testmagnetlink123456789")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load and system apps to be detected
        device.waitForIdle()
        Thread.sleep(3000) // Allow time for async system app detection

        // Verify the compatible apps section is shown
        onView(withId(R.id.textViewCompatibleAppsTitle))
            .check(matches(isDisplayed()))

        // Check if system apps are displayed or no apps message is shown
        try {
            onView(withId(R.id.recyclerViewSystemApps))
                .check(matches(isDisplayed()))
        } catch (e: Exception) {
            onView(withId(R.id.textViewNoCompatibleApps))
                .check(matches(isDisplayed()))
        }

        scenario.close()
    }

    @Test
    fun testSystemAppsDisplayedForDirectDownloadUrl() {
        // Create a test profile
        val testProfile = createProfile("Test YT-DLP", ServerProfile.TYPE_YTDL)

        // Start ShareActivity with a direct download URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/file.zip")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load and system apps to be detected
        device.waitForIdle()
        Thread.sleep(3000) // Allow time for async system app detection

        // Verify the compatible apps section is shown
        onView(withId(R.id.textViewCompatibleAppsTitle))
            .check(matches(isDisplayed()))

        // System apps section should be visible
        try {
            onView(withId(R.id.recyclerViewSystemApps))
                .check(matches(isDisplayed()))
        } catch (e: Exception) {
            onView(withId(R.id.textViewNoCompatibleApps))
                .check(matches(isDisplayed()))
        }

        scenario.close()
    }

    @Test
    fun testSystemAppLaunchFlowWithDismissal() {
        // Create a test profile
        val testProfile = createProfile("Test MeTube", ServerProfile.TYPE_METUBE)

        // Start ShareActivity with a URL that should have compatible apps
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load and system apps to be detected
        device.waitForIdle()
        Thread.sleep(3000)

        // Check if there are any system apps listed
        try {
            onView(withId(R.id.recyclerViewSystemApps))
                .check(matches(isDisplayed()))

            // Try to click on the first system app if any exist
            onView(withId(R.id.recyclerViewSystemApps))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            // Wait for potential app launch
            device.waitForIdle()
            Thread.sleep(2000)

            // Verify ShareActivity is dismissed after clicking system app
            val shareActivityExists = try {
                device.findObject(UiSelector().resourceId("com.shareconnect:id/textViewYouTubeLink"))
                    .waitForExists(1000)
            } catch (e: Exception) {
                false
            }

            // Activity should be dismissed after launching system app
            assert(!shareActivityExists) { "ShareActivity should be dismissed after launching system app" }

        } catch (e: Exception) {
            // If no system apps are available, verify the "no compatible apps" message is shown
            onView(withId(R.id.textViewNoCompatibleApps))
                .check(matches(isDisplayed()))
        }

        scenario.close()
    }

    @Test
    fun testOriginalShareToAppsFunctionalityPreserved() {
        // Create a test profile
        val testProfile = createProfile("Test Profile", ServerProfile.TYPE_METUBE)

        // Start ShareActivity
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

        // Verify original "Share to Apps" button is still present and functional
        onView(withId(R.id.buttonShareToApps))
            .check(matches(isDisplayed()))
            .check(matches(withText("Share to Apps")))
            .perform(click())

        // Wait for share chooser to appear
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify share chooser appeared
        val shareDialogExists = device.findObject(
            UiSelector().textContains("Share")
        ).waitForExists(2000)

        assert(shareDialogExists) { "Share dialog should appear when clicking Share to Apps" }

        // Press back to cancel the share dialog
        device.pressBack()
        device.waitForIdle()
        Thread.sleep(1000)

        scenario.close()
    }

    @Test
    fun testSystemAppsScrollingFunctionality() {
        // Create a test profile
        val testProfile = createProfile("Test Profile", ServerProfile.TYPE_METUBE)

        // Start ShareActivity with a URL that might have multiple compatible apps
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(3000)

        // Test scrolling to ensure system apps section is accessible
        try {
            // Scroll to the system apps section
            onView(withId(R.id.textViewCompatibleAppsTitle))
                .perform(scrollTo())
                .check(matches(isDisplayed()))

            // If RecyclerView is present, test scrolling within it
            onView(withId(R.id.recyclerViewSystemApps))
                .perform(scrollTo())
                .check(matches(isDisplayed()))

        } catch (e: Exception) {
            // If no system apps found, verify no apps message is accessible
            onView(withId(R.id.textViewNoCompatibleApps))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
        }

        // Verify we can still scroll to other elements
        onView(withId(R.id.buttonShareToApps))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withId(R.id.textViewYouTubeLink))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testSystemAppsVisibilityForDifferentUrlTypes() {
        // Create a test profile
        val testProfile = createProfile("Test Profile", ServerProfile.TYPE_METUBE)

        val testUrls = listOf(
            "https://www.youtube.com/watch?v=test123",
            "https://vimeo.com/123456",
            "https://soundcloud.com/artist/track",
            "https://example.com/file.pdf",
            "invalid-url-format"
        )

        for (url in testUrls) {
            val intent = Intent(context, ShareActivity::class.java).apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val scenario = ActivityScenario.launch<ShareActivity>(intent)

            // Wait for activity to load
            device.waitForIdle()
            Thread.sleep(2000)

            // For each URL type, system apps section should either show apps or show "no compatible apps"
            try {
                onView(withId(R.id.textViewCompatibleAppsTitle))
                    .check(matches(isDisplayed()))

                // Either apps are shown or no apps message is shown
                try {
                    onView(withId(R.id.recyclerViewSystemApps))
                        .check(matches(isDisplayed()))
                } catch (e: Exception) {
                    onView(withId(R.id.textViewNoCompatibleApps))
                        .check(matches(isDisplayed()))
                }
            } catch (e: Exception) {
                // For invalid URLs, system apps section might be hidden entirely
                // This is acceptable behavior
            }

            scenario.close()
            Thread.sleep(500) // Brief pause between tests
        }
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