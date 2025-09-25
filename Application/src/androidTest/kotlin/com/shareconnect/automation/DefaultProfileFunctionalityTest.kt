package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.SCApplication
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultProfileFunctionalityTest {
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

        // Clear existing profiles
        profileManager.profiles.forEach { profile ->
            profileManager.deleteProfile(profile)
        }

        // Create test profiles
        createTestProfiles()

        device.waitForIdle()
    }

    @After
    fun tearDown() {
        device.pressHome()
    }

    private fun createTestProfiles() {
        val profile1 = com.shareconnect.ServerProfile(
            null,
            "MeTube Server",
            "http://192.168.1.100",
            8081,
            true, // Make this the default initially
            com.shareconnect.ServerProfile.TYPE_METUBE,
            null
        )

        val profile2 = com.shareconnect.ServerProfile(
            null,
            "qBittorrent Server",
            "http://192.168.1.101",
            8080,
            false,
            com.shareconnect.ServerProfile.TYPE_TORRENT,
            com.shareconnect.ServerProfile.TORRENT_CLIENT_QBITTORRENT
        )

        val profile3 = com.shareconnect.ServerProfile(
            null,
            "jDownloader Server",
            "http://192.168.1.102",
            9666,
            false,
            com.shareconnect.ServerProfile.TYPE_JDOWNLOADER,
            null
        )

        profileManager.addProfile(profile1)
        profileManager.addProfile(profile2)
        profileManager.addProfile(profile3)
    }

    private fun launchApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), LAUNCH_TIMEOUT)

        // Wait for splash screen to complete
        Thread.sleep(2500)
    }

    private fun navigateToProfilesList() {
        // Navigate to Profiles
        val profilesButton = device.findObject(By.res(PACKAGE_NAME, "buttonManageProfiles"))
        assertNotNull("Profiles button should exist", profilesButton)
        profilesButton.click()
        Thread.sleep(1000)

        // Verify we're at Profiles activity
        val fab = device.wait(
            Until.findObject(By.res(PACKAGE_NAME, "fab")),
            TIMEOUT
        )
        assertNotNull("Should be at ProfilesActivity", fab)
    }

    @Test
    fun testDefaultProfileIsIndicatedWithGoldenStar() {
        launchApp()
        navigateToProfilesList()

        // Look for the default profile (MeTube Server)
        val defaultProfileCard = device.findObject(By.text("MeTube Server"))
        assertNotNull("Default profile should be visible", defaultProfileCard)

        // Find the golden star icon indicating default profile
        val goldenStar = device.findObject(By.res(PACKAGE_NAME, "imageViewDefault"))
        assertNotNull("Golden star should be visible for default profile", goldenStar)
        assertTrue("Golden star should be visible", goldenStar.exists())

        // Check that the default button shows "Default Profile" and is disabled
        val defaultButton = device.findObject(By.text("Default Profile"))
        assertNotNull("Default button should show 'Default Profile' text", defaultButton)
        assertFalse("Default button should be disabled for current default profile",
                   defaultButton.isEnabled)
    }

    @Test
    fun testNonDefaultProfilesShowOutlineStarButton() {
        launchApp()
        navigateToProfilesList()

        // Look for non-default profiles and verify they have outline star buttons
        val qbittorrentProfile = device.findObject(By.text("qBittorrent Server"))
        assertNotNull("qBittorrent profile should be visible", qbittorrentProfile)

        val jdownloaderProfile = device.findObject(By.text("jDownloader Server"))
        assertNotNull("jDownloader profile should be visible", jdownloaderProfile)

        // Check that non-default profiles have "Set as Default" buttons that are enabled
        val setDefaultButtons = device.findObjects(By.text("Set as Default"))
        assertTrue("Should have at least 2 'Set as Default' buttons for non-default profiles",
                  setDefaultButtons.size >= 2)

        // Verify the buttons are enabled
        setDefaultButtons.forEach { button ->
            assertTrue("Set default buttons should be enabled", button.isEnabled)
        }
    }

    @Test
    fun testChangingDefaultProfile() {
        launchApp()
        navigateToProfilesList()

        // Verify initial state - MeTube is default
        val initialDefaultButton = device.findObject(By.text("Default Profile"))
        assertNotNull("Initial default button should exist", initialDefaultButton)

        // Find qBittorrent profile's "Set as Default" button
        val qbittorrentProfile = device.findObject(By.text("qBittorrent Server"))
        assertNotNull("qBittorrent profile should be visible", qbittorrentProfile)

        // Look for "Set as Default" button near qBittorrent profile
        // We need to find the button within the same card as the profile
        val setDefaultButtons = device.findObjects(By.text("Set as Default"))
        assertTrue("Should have Set as Default buttons", setDefaultButtons.isNotEmpty())

        // Click the first "Set as Default" button (should be for a non-default profile)
        val firstSetDefaultButton = setDefaultButtons[0]
        assertTrue("Button should be enabled", firstSetDefaultButton.isEnabled)
        firstSetDefaultButton.click()
        Thread.sleep(1000)

        // Handle potential confirmation dialog
        val confirmButton = device.findObject(By.text("OK"))
        if (confirmButton != null && confirmButton.exists()) {
            confirmButton.click()
            Thread.sleep(1000)
        }

        // Verify that the profile is now marked as default
        // The previously default profile should now have a "Set as Default" button
        val newSetDefaultButtons = device.findObjects(By.text("Set as Default"))
        val newDefaultButtons = device.findObjects(By.text("Default Profile"))

        // We should now have exactly one "Default Profile" button (the newly set default)
        // and the rest should be "Set as Default" buttons
        assertEquals("Should have exactly one default profile", 1, newDefaultButtons.size)
        assertTrue("Should have multiple set default buttons for non-default profiles",
                  newSetDefaultButtons.size >= 2)

        // Verify the golden star moved to the new default profile
        val goldenStars = device.findObjects(By.res(PACKAGE_NAME, "imageViewDefault"))
        assertEquals("Should have exactly one golden star", 1, goldenStars.size)
    }

    @Test
    fun testDefaultProfileChangePersistsAcrossAppRestarts() {
        launchApp()
        navigateToProfilesList()

        // Change default to jDownloader profile
        val setDefaultButtons = device.findObjects(By.text("Set as Default"))
        assertTrue("Should have Set as Default buttons", setDefaultButtons.isNotEmpty())

        // Click the last "Set as Default" button (should be for jDownloader)
        val lastSetDefaultButton = setDefaultButtons[setDefaultButtons.size - 1]
        lastSetDefaultButton.click()
        Thread.sleep(1000)

        // Handle potential confirmation dialog
        val confirmButton = device.findObject(By.text("OK"))
        if (confirmButton != null && confirmButton.exists()) {
            confirmButton.click()
            Thread.sleep(1000)
        }

        // Verify jDownloader is now default
        val jdownloaderDefaultButton = device.findObject(By.text("Default Profile"))
        assertNotNull("jDownloader should now be default", jdownloaderDefaultButton)

        // Restart the app
        device.pressBack() // Go back to main
        Thread.sleep(500)
        device.pressBack() // Close app
        Thread.sleep(1000)

        // Relaunch app
        launchApp()
        navigateToProfilesList()

        // Verify the default profile persisted
        val persistedDefaultButtons = device.findObjects(By.text("Default Profile"))
        assertEquals("Should still have exactly one default profile after restart",
                    1, persistedDefaultButtons.size)

        val persistedGoldenStars = device.findObjects(By.res(PACKAGE_NAME, "imageViewDefault"))
        assertEquals("Should still have exactly one golden star after restart",
                    1, persistedGoldenStars.size)
    }

    @Test
    fun testDefaultProfileButtonVisualStates() {
        launchApp()
        navigateToProfilesList()

        // Check that default profile button has filled star icon
        val defaultButtons = device.findObjects(By.text("Default Profile"))
        assertEquals("Should have exactly one default button", 1, defaultButtons.size)

        // Check that non-default profile buttons have outline star icons
        val setDefaultButtons = device.findObjects(By.text("Set as Default"))
        assertTrue("Should have multiple set default buttons", setDefaultButtons.size >= 2)

        // Verify visual states by checking button enabled states
        defaultButtons.forEach { button ->
            assertFalse("Default profile button should be disabled", button.isEnabled)
        }

        setDefaultButtons.forEach { button ->
            assertTrue("Set default buttons should be enabled", button.isEnabled)
        }
    }

    @Test
    fun testMainActivityReflectsCorrectDefaultProfile() {
        launchApp()

        // Check the default service button text on MainActivity
        val defaultServiceButton = device.findObject(By.res(PACKAGE_NAME, "buttonOpenMeTube"))
        assertNotNull("Default service button should exist", defaultServiceButton)

        val buttonText = defaultServiceButton.text
        assertTrue("Button should show default profile name",
                  buttonText.contains("MeTube Server") || buttonText.contains("Open MeTube Server"))

        // Change the default profile
        navigateToProfilesList()
        val setDefaultButtons = device.findObjects(By.text("Set as Default"))
        if (setDefaultButtons.isNotEmpty()) {
            setDefaultButtons[0].click()
            Thread.sleep(1000)

            // Handle potential confirmation dialog
            val confirmButton = device.findObject(By.text("OK"))
            if (confirmButton != null && confirmButton.exists()) {
                confirmButton.click()
                Thread.sleep(1000)
            }
        }

        // Go back to MainActivity
        device.pressBack()
        Thread.sleep(1000)

        // Verify the button text changed to reflect the new default
        val updatedButton = device.findObject(By.res(PACKAGE_NAME, "buttonOpenMeTube"))
        assertNotNull("Updated service button should exist", updatedButton)

        val newButtonText = updatedButton.text
        assertNotEquals("Button text should have changed", buttonText, newButtonText)
        assertTrue("Button should now show different profile name",
                  newButtonText.contains("qBittorrent") || newButtonText.contains("jDownloader"))
    }
}