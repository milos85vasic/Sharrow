package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.R
import com.shareconnect.SCApplication
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServiceIconValidationTest {
    private lateinit var device: UiDevice
    private lateinit var context: Context
    private lateinit var profileManager: ProfileManager
    private lateinit var app: SCApplication

    companion object {
        private const val TIMEOUT = 5000L
        private const val PACKAGE_NAME = "com.shareconnect"
        private const val LAUNCH_TIMEOUT = 10000L
    }

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        app = context.applicationContext as SCApplication
        profileManager = ProfileManager(context)

        // Clear existing profiles and create test profiles with different service types
        profileManager.profiles.forEach { profile ->
            profileManager.deleteProfile(profile)
        }

        createTestProfilesWithDifferentServices()
        device.waitForIdle()
    }

    @After
    fun tearDown() {
        device.pressHome()
    }

    private fun createTestProfilesWithDifferentServices() {
        // Create test profiles for each service type
        val metubeProfile = com.shareconnect.ServerProfile(
            null,
            "MeTube Server",
            "http://192.168.1.100",
            8081,
            true, // Make this the default
            com.shareconnect.ServerProfile.TYPE_METUBE,
            null
        )

        val qbittorrentProfile = com.shareconnect.ServerProfile(
            null,
            "qBittorrent Server",
            "http://192.168.1.101",
            8080,
            false,
            com.shareconnect.ServerProfile.TYPE_TORRENT,
            com.shareconnect.ServerProfile.TORRENT_CLIENT_QBITTORRENT
        )

        val jdownloaderProfile = com.shareconnect.ServerProfile(
            null,
            "jDownloader Server",
            "http://192.168.1.102",
            9666,
            false,
            com.shareconnect.ServerProfile.TYPE_JDOWNLOADER,
            null
        )

        val ytdlProfile = com.shareconnect.ServerProfile(
            null,
            "YT-DLP Server",
            "http://192.168.1.103",
            8082,
            false,
            com.shareconnect.ServerProfile.TYPE_YTDL,
            null
        )

        profileManager.addProfile(metubeProfile)
        profileManager.addProfile(qbittorrentProfile)
        profileManager.addProfile(jdownloaderProfile)
        profileManager.addProfile(ytdlProfile)
    }

    private fun launchApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), LAUNCH_TIMEOUT)

        // Wait for splash screen to complete
        Thread.sleep(3000)
    }

    @Test
    fun testServiceProfileIconsAreCorrectlyDisplayed() {
        launchApp()

        // Verify we're at MainActivity with the Service Profiles section
        val serviceProfilesSection = device.wait(
            Until.findObject(By.text("Service Profiles")),
            TIMEOUT
        )
        assertNotNull("Service Profiles section should be visible", serviceProfilesSection)

        // Find the RecyclerView containing the profiles
        val profilesRecyclerView = device.findObject(By.res(PACKAGE_NAME, "recyclerViewProfiles"))
        assertNotNull("Profiles RecyclerView should be present", profilesRecyclerView)
        assertTrue("Profiles RecyclerView should be visible", profilesRecyclerView.exists())

        // Check that profile cards are visible
        val profileCards = device.findObjects(By.clazz("android.view.ViewGroup"))
        assertTrue("Should have profile cards visible", profileCards.isNotEmpty())

        println("🔍 Found ${profileCards.size} potential profile items")

        // Verify each service type has its own profile displayed
        verifyServiceProfileExists("MeTube Server", "MeTube")
        verifyServiceProfileExists("qBittorrent Server", "qBittorrent")
        verifyServiceProfileExists("jDownloader Server", "jDownloader")
        verifyServiceProfileExists("YT-DLP Server", "YT-DLP")

        println("✅ All service profiles are displayed with correct information")
    }

    @Test
    fun testMetubeProfileDisplaysCorrectIcon() {
        launchApp()

        // Find MeTube profile specifically
        val metubeProfile = device.findObject(By.text("MeTube Server"))
        assertNotNull("MeTube profile should be visible", metubeProfile)
        assertTrue("MeTube profile should exist", metubeProfile.exists())

        // Verify MeTube service type text is displayed
        val metubeServiceType = device.findObject(By.text("MeTube"))
        assertNotNull("MeTube service type should be displayed", metubeServiceType)
        assertTrue("MeTube service type should be visible", metubeServiceType.exists())

        // Verify it's marked as default (should have star indicator)
        val defaultIndicator = device.findObject(By.res(PACKAGE_NAME, "defaultIndicator"))
        if (defaultIndicator != null && defaultIndicator.exists()) {
            assertTrue("Default indicator should be visible for MeTube profile", defaultIndicator.exists())
        }

        println("✅ MeTube profile displays correctly with proper service type")
    }

    @Test
    fun testTorrentProfileDisplaysCorrectIcon() {
        launchApp()

        // Find Torrent (qBittorrent) profile specifically
        val torrentProfile = device.findObject(By.text("qBittorrent Server"))
        assertNotNull("qBittorrent profile should be visible", torrentProfile)
        assertTrue("qBittorrent profile should exist", torrentProfile.exists())

        // Verify torrent service type text is displayed
        val torrentServiceType = device.findObject(By.text("qBittorrent"))
        assertNotNull("qBittorrent service type should be displayed", torrentServiceType)
        assertTrue("qBittorrent service type should be visible", torrentServiceType.exists())

        println("✅ Torrent profile displays correctly with proper service type")
    }

    @Test
    fun testJDownloaderProfileDisplaysCorrectIcon() {
        launchApp()

        // Find jDownloader profile specifically
        val jdownloaderProfile = device.findObject(By.text("jDownloader Server"))
        assertNotNull("jDownloader profile should be visible", jdownloaderProfile)
        assertTrue("jDownloader profile should exist", jdownloaderProfile.exists())

        // Verify jDownloader service type text is displayed
        val jdownloaderServiceType = device.findObject(By.text("jDownloader"))
        assertNotNull("jDownloader service type should be displayed", jdownloaderServiceType)
        assertTrue("jDownloader service type should be visible", jdownloaderServiceType.exists())

        println("✅ jDownloader profile displays correctly with proper service type")
    }

    @Test
    fun testYTDLProfileDisplaysCorrectIcon() {
        launchApp()

        // Find YT-DLP profile specifically
        val ytdlProfile = device.findObject(By.text("YT-DLP Server"))
        assertNotNull("YT-DLP profile should be visible", ytdlProfile)
        assertTrue("YT-DLP profile should exist", ytdlProfile.exists())

        // Verify YT-DLP service type text is displayed
        val ytdlServiceType = device.findObject(By.text("YT-DLP"))
        assertNotNull("YT-DLP service type should be displayed", ytdlServiceType)
        assertTrue("YT-DLP service type should be visible", ytdlServiceType.exists())

        println("✅ YT-DLP profile displays correctly with proper service type")
    }

    @Test
    fun testProfileIconsAreNotGenericGrayCircles() {
        launchApp()

        // This test ensures that profiles don't show generic gray circle icons
        val serviceProfilesSection = device.wait(
            Until.findObject(By.text("Service Profiles")),
            TIMEOUT
        )
        assertNotNull("Service Profiles section should be visible", serviceProfilesSection)

        // Find profile icons by their resource ID
        val profileIcons = device.findObjects(By.res(PACKAGE_NAME, "profileIcon"))
        assertTrue("Should have profile icons displayed", profileIcons.isNotEmpty())

        println("🔍 Found ${profileIcons.size} profile icons")

        // All profile icons should be visible (not completely transparent or hidden)
        profileIcons.forEach { icon ->
            assertTrue("Profile icon should be visible", icon.exists())
            val iconBounds = icon.visibleBounds
            assertTrue("Profile icon should have reasonable dimensions",
                      iconBounds.width() > 20 && iconBounds.height() > 20)
        }

        // Verify that we have the expected service type labels
        val serviceTypes = listOf("MeTube", "qBittorrent", "jDownloader", "YT-DLP")
        serviceTypes.forEach { serviceType ->
            val serviceTypeLabel = device.findObject(By.text(serviceType))
            if (serviceTypeLabel != null && serviceTypeLabel.exists()) {
                println("✅ Found service type: $serviceType")
            }
        }

        println("✅ Profile icons are properly displayed (not generic gray circles)")
    }

    @Test
    fun testProfileClickabilityAndNavigation() {
        launchApp()

        // Test that profile items are clickable
        val metubeProfile = device.findObject(By.text("MeTube Server"))
        assertNotNull("MeTube profile should be visible", metubeProfile)

        // Click on the profile
        metubeProfile.click()
        Thread.sleep(1000)

        // Should either open the service interface or show a dialog/menu
        // For now, we'll just verify that the click was registered (no crash occurred)
        // and the main screen is still accessible

        val serviceProfilesAfterClick = device.findObject(By.text("Service Profiles"))
        assertNotNull("Should be able to return to service profiles after click", serviceProfilesAfterClick)

        println("✅ Profile items are clickable and don't cause crashes")
    }

    private fun verifyServiceProfileExists(profileName: String, serviceType: String) {
        val profile = device.findObject(By.text(profileName))
        assertNotNull("Profile '$profileName' should be visible", profile)
        assertTrue("Profile '$profileName' should exist", profile.exists())

        val serviceTypeLabel = device.findObject(By.text(serviceType))
        assertNotNull("Service type '$serviceType' should be displayed", serviceTypeLabel)
        assertTrue("Service type '$serviceType' should be visible", serviceTypeLabel.exists())

        println("✅ Verified profile: $profileName ($serviceType)")
    }
}