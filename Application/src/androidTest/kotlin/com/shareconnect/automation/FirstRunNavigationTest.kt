package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.MainActivity
import com.shareconnect.ProfileManager
import com.shareconnect.SCApplication
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.fail

@RunWith(AndroidJUnit4::class)
class FirstRunNavigationTest {
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
        // Initialize UiDevice
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        app = context.applicationContext as SCApplication
        profileManager = ProfileManager(context)

        // Clear all data to simulate first run
        clearAppData()

        // Wait for any previous instances to close
        device.waitForIdle()
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        // Clean up after test
        device.pressHome()
    }

    private fun clearAppData() {
        // Clear all profiles to simulate first run
        profileManager.profiles.forEach { profile ->
            profileManager.deleteProfile(profile)
        }

        // Clear shared preferences
        context.getSharedPreferences("MeTubeSharePrefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Clear theme preferences
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun launchApp() {
        // Launch the app from scratch
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)

        // Wait for app to appear
        device.wait(
            Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun testFirstRunNavigationWithoutProfileCreation() {
        // Launch app for first time
        launchApp()

        // Wait for splash screen to complete (2 seconds)
        Thread.sleep(2500)

        // Should redirect to Settings Activity
        val settingsTitle = device.wait(
            Until.findObject(By.text("Settings")),
            TIMEOUT
        )
        assertNotNull("Settings screen should appear on first run", settingsTitle)

        // Log current activity for debugging
        val currentActivity = device.currentPackageName
        println("Current package before back press: $currentActivity")

        // Press back button once
        device.pressBack()

        // Wait a moment
        Thread.sleep(1000)

        // Check if app is still running
        val isAppStillRunning = device.hasObject(By.pkg(PACKAGE_NAME))

        if (isAppStillRunning) {
            // Log what's visible
            val visibleText = device.findObject(By.pkg(PACKAGE_NAME))
            println("App still visible after first back press. Visible elements: ${visibleText?.text}")

            // Try pressing back again to see how many it takes
            var additionalPresses = 0
            while (additionalPresses < 5 && device.hasObject(By.pkg(PACKAGE_NAME))) {
                device.pressBack()
                additionalPresses++
                Thread.sleep(500)
            }

            fail("App should close with single back press but required ${1 + additionalPresses} presses")
        }

        assertTrue("App should close with single back press when no profiles created", !isAppStillRunning)

        // Verify the app is not running in background
        device.pressRecentApps()
        Thread.sleep(1000)

        val appInRecents = device.findObject(By.text("ShareConnect"))
        if (appInRecents != null) {
            // If app is in recents, it should not be active
            appInRecents.click()
            Thread.sleep(1000)

            // Should start from Settings again since no profiles exist
            val settingsAgain = device.wait(
                Until.findObject(By.text("Settings")),
                TIMEOUT
            )
            assertNotNull("App should restart at Settings if no profiles", settingsAgain)
        }

        device.pressHome()
    }

    @Test
    fun testFirstRunNavigationWithProfileCreation() {
        // Launch app for first time
        launchApp()

        // Should redirect to Settings Activity
        val settingsTitle = device.wait(
            Until.findObject(By.text("Settings")),
            TIMEOUT
        )
        assertNotNull("Settings screen should appear on first run", settingsTitle)

        // Navigate to Server Profiles
        val profilesOption = device.findObject(
            UiSelector().text("Server Profiles")
                .className("android.widget.TextView")
        )
        if (profilesOption != null && profilesOption.exists()) {
            profilesOption.click()
            Thread.sleep(1000)

            // Click Add Profile button (FAB)
            val addButton = device.findObject(
                UiSelector().resourceId("$PACKAGE_NAME:id/fab")
            )
            if (addButton != null && addButton.exists()) {
                addButton.click()
                Thread.sleep(1000)

                // Fill in profile details
                fillProfileDetails()

                // Save profile
                val saveButton = device.findObject(
                    UiSelector().resourceId("$PACKAGE_NAME:id/buttonSave")
                )
                if (saveButton != null && saveButton.exists()) {
                    saveButton.click()
                    Thread.sleep(1000)
                }

                // Navigate back to Settings
                device.pressBack()
                Thread.sleep(500)
            }
        }

        // Press back from Settings
        device.pressBack()
        Thread.sleep(500)

        // Should now be at MainActivity
        val mainActivityElement = device.wait(
            Until.findObject(By.res(PACKAGE_NAME, "buttonOpenMeTube")),
            TIMEOUT
        )
        assertNotNull("Should navigate to MainActivity after creating profile", mainActivityElement)

        // Press back once more to close app
        device.pressBack()
        Thread.sleep(500)

        // App should be closed
        val isAppClosed = device.wait(
            Until.gone(By.pkg(PACKAGE_NAME)),
            2000L
        )
        assertTrue("App should close with single back press from MainActivity", isAppClosed)
    }

    @Test
    fun testThemeChangeDoesNotDuplicateActivity() {
        // First create a profile so we don't get stuck in first-run
        createTestProfile()

        // Launch app
        launchApp()

        // Navigate to Settings
        val settingsButton = device.findObject(
            UiSelector().resourceId("$PACKAGE_NAME:id/buttonSettings")
        )
        if (settingsButton != null && settingsButton.exists()) {
            settingsButton.click()
            Thread.sleep(1000)
        }

        // Navigate to Theme Selection
        val themeOption = device.findObject(
            UiSelector().text("Theme")
                .className("android.widget.TextView")
        )
        if (themeOption != null && themeOption.exists()) {
            themeOption.click()
            Thread.sleep(1000)

            // Select a different theme
            val themeItem = device.findObject(
                UiSelector().text("Crimson")
                .className("android.widget.TextView")
            )
            if (themeItem != null && themeItem.exists()) {
                themeItem.click()
                Thread.sleep(2000) // Wait for theme change and activity recreation
            }
        }

        // Now press back - should go directly to MainActivity
        device.pressBack()
        Thread.sleep(500)

        // Verify we're at MainActivity
        val mainActivityElement = device.findObject(
            UiSelector().resourceId("$PACKAGE_NAME:id/buttonOpenMeTube")
        )
        assertTrue("Should be at MainActivity after theme change",
                  mainActivityElement != null && mainActivityElement.exists())

        // Press back once more - app should close
        device.pressBack()
        Thread.sleep(500)

        // Verify app is closed
        val isAppClosed = device.wait(
            Until.gone(By.pkg(PACKAGE_NAME)),
            2000L
        )
        assertTrue("App should close with single back press after theme change", isAppClosed)
    }

    @Test
    fun testNoDoubleSettingsOnFirstRun() {
        // Clear data to ensure first run
        clearAppData()

        // Launch app
        launchApp()

        // Should show Settings
        val settingsTitle = device.wait(
            Until.findObject(By.text("Settings")),
            TIMEOUT
        )
        assertNotNull("Settings should appear on first run", settingsTitle)

        // Track the number of back presses needed to exit
        var backPressCount = 0
        val maxBackPresses = 10 // Increased to catch the 4-5 press issue

        while (backPressCount < maxBackPresses) {
            device.pressBack()
            backPressCount++
            Thread.sleep(500)

            // Check if app is still visible
            val appStillVisible = device.hasObject(By.pkg(PACKAGE_NAME))
            if (!appStillVisible) {
                break
            }
        }

        // Assert that only one back press was needed
        assertEquals("Should only need ONE back press to exit from first-run Settings, but needed $backPressCount",
                    1, backPressCount)
    }

    @Test
    fun testAppDoesNotAutoRelaunchAfterKill() {
        // Clear data to ensure first run
        clearAppData()

        // Launch app
        launchApp()

        // Should show Settings (first run)
        val settingsTitle = device.wait(
            Until.findObject(By.text("Settings")),
            TIMEOUT
        )
        assertNotNull("Settings should appear on first run", settingsTitle)

        // Kill the app using back button
        device.pressBack()
        Thread.sleep(1000)

        // Verify app is closed
        val isAppClosed = device.wait(
            Until.gone(By.pkg(PACKAGE_NAME)),
            2000L
        )
        assertTrue("App should be closed after back press", isAppClosed)

        // Wait to see if app relaunches itself
        Thread.sleep(3000)

        // Check that app has NOT relaunched
        val appRelaunched = device.hasObject(By.pkg(PACKAGE_NAME))
        assertFalse("App should NOT auto-relaunch after being killed", appRelaunched)

        // Also verify through recent apps
        device.pressRecentApps()
        Thread.sleep(1000)

        // If app is in recents, it should not be running
        val appInRecents = device.findObject(By.text("ShareConnect"))
        if (appInRecents != null) {
            // Clear it from recents by swiping
            val bounds = appInRecents.visibleBounds
            device.swipe(bounds.centerX(), bounds.centerY(), bounds.left - 100, bounds.centerY(), 10)
            Thread.sleep(500)
        }

        // Go back to home
        device.pressHome()
        Thread.sleep(1000)

        // One more check - app should still not be running
        val appStillNotRunning = !device.hasObject(By.pkg(PACKAGE_NAME))
        assertTrue("App should remain closed and not auto-restart", appStillNotRunning)
    }

    @Test
    fun testAppProperlyClosesWithoutProfiles() {
        // Clear all data
        clearAppData()

        // Launch app
        launchApp()

        // Should redirect to Settings
        val settingsVisible = device.wait(
            Until.findObject(By.text("Settings")),
            TIMEOUT
        )
        assertNotNull("Should show Settings on first run", settingsVisible)

        // Force stop the app (simulating user killing it from task manager)
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
        Thread.sleep(1000)

        // Verify app is completely stopped
        val processOutput = device.executeShellCommand("ps | grep $PACKAGE_NAME")
        assertTrue("App process should be killed", processOutput.isEmpty() || !processOutput.contains(PACKAGE_NAME))

        // Wait and ensure app doesn't restart
        Thread.sleep(5000)

        // Check app is not running
        assertFalse("App should not auto-restart after force stop", device.hasObject(By.pkg(PACKAGE_NAME)))

        // Manually launch app again to verify it works normally
        launchApp()

        // Should show Settings again (still no profiles)
        val settingsAgain = device.wait(
            Until.findObject(By.text("Settings")),
            TIMEOUT
        )
        assertNotNull("App should launch normally when started manually", settingsAgain)
    }

    private fun fillProfileDetails() {
        // Fill in name field
        val nameField = device.findObject(
            UiSelector().resourceId("$PACKAGE_NAME:id/editTextName")
        )
        if (nameField != null && nameField.exists()) {
            nameField.text = "Test MeTube"
        }

        // Fill in URL field
        val urlField = device.findObject(
            UiSelector().resourceId("$PACKAGE_NAME:id/editTextUrl")
        )
        if (urlField != null && urlField.exists()) {
            urlField.text = "http://192.168.1.100"
        }

        // Fill in port field
        val portField = device.findObject(
            UiSelector().resourceId("$PACKAGE_NAME:id/editTextPort")
        )
        if (portField != null && portField.exists()) {
            portField.text = "8081"
        }

        // Select service type
        val serviceSpinner = device.findObject(
            UiSelector().resourceId("$PACKAGE_NAME:id/spinnerServiceType")
        )
        if (serviceSpinner != null && serviceSpinner.exists()) {
            serviceSpinner.click()
            Thread.sleep(500)

            val metubeOption = device.findObject(
                UiSelector().text("MeTube")
            )
            if (metubeOption != null && metubeOption.exists()) {
                metubeOption.click()
                Thread.sleep(500)
            }
        }
    }

    private fun createTestProfile() {
        // Create a test profile programmatically to bypass first-run
        val profile = com.shareconnect.ServerProfile(
            null, // id
            "Test Profile", // name
            "http://192.168.1.100", // url
            8081, // port
            true, // isDefault
            com.shareconnect.ServerProfile.TYPE_METUBE, // serviceType
            null // torrentClientType
        )
        profileManager.addProfile(profile)
    }
}