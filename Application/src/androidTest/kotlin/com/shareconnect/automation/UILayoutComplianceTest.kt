package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import android.graphics.Rect
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
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
class UILayoutComplianceTest {
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

        // Ensure we have at least one profile for testing
        if (!profileManager.hasProfiles()) {
            createTestProfile()
        }

        device.waitForIdle()
    }

    @After
    fun tearDown() {
        device.pressHome()
    }

    private fun createTestProfile() {
        val profile = com.shareconnect.ServerProfile(
            null,
            "Test Profile",
            "http://192.168.1.100",
            8081,
            true,
            com.shareconnect.ServerProfile.TYPE_METUBE,
            null
        )
        profileManager.addProfile(profile)
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

    private fun checkLayoutCompliance(activityName: String) {
        // Get screen dimensions
        val displaySize = device.displaySizeDp
        val screenBounds = Rect(0, 0, displaySize.x, displaySize.y)

        // Find the main content area (should not overlap with system bars)
        val mainContent = device.findObject(By.pkg(PACKAGE_NAME))
        if (mainContent == null) {
            fail("No main content found for $activityName")
            return
        }

        val contentBounds = mainContent.visibleBounds

        // Check if content fits properly between system bars
        // The content should not be at the very top (0) or very bottom of screen
        if (contentBounds.top <= 50) { // Allowing small margin for status bar
            fail("$activityName: Content appears to be overlapping with status bar. Top: ${contentBounds.top}")
        }

        // Check for toolbar presence
        val toolbar = device.findObject(By.res(PACKAGE_NAME, "toolbar"))
        if (toolbar != null && toolbar.exists()) {
            val toolbarBounds = toolbar.visibleBounds

            // Ensure toolbar is not overlapping with system bars
            if (toolbarBounds.top <= 50) {
                fail("$activityName: Toolbar appears to be overlapping with status bar. Top: ${toolbarBounds.top}")
            }
        }

        // Check for RecyclerView if present
        val recyclerView = device.findObject(By.clazz("androidx.recyclerview.widget.RecyclerView"))
        if (recyclerView != null && recyclerView.exists()) {
            val recyclerBounds = recyclerView.visibleBounds

            // RecyclerView should have proper padding and not extend to screen edges
            if (recyclerBounds.bottom >= screenBounds.bottom - 50) { // Allow margin for navigation
                fail("$activityName: RecyclerView extends too close to navigation bar. Bottom: ${recyclerBounds.bottom}, Screen: ${screenBounds.bottom}")
            }
        }

        // Check for ScrollView if present
        val scrollView = device.findObject(By.clazz("android.widget.ScrollView"))
        if (scrollView != null && scrollView.exists()) {
            val scrollBounds = scrollView.visibleBounds

            // ScrollView should have proper padding
            if (scrollBounds.bottom >= screenBounds.bottom - 50) {
                fail("$activityName: ScrollView extends too close to navigation bar. Bottom: ${scrollBounds.bottom}, Screen: ${screenBounds.bottom}")
            }
        }

        println("✓ $activityName: Layout compliance check passed")
    }

    @Test
    fun testMainActivityLayoutCompliance() {
        launchApp()

        // Should be at MainActivity
        val mainButton = device.wait(
            Until.findObject(By.res(PACKAGE_NAME, "buttonOpenMeTube")),
            TIMEOUT
        )
        assertNotNull("Should be at MainActivity", mainButton)

        checkLayoutCompliance("MainActivity")
    }

    @Test
    fun testSettingsActivityLayoutCompliance() {
        launchApp()

        // Navigate to Settings
        val settingsButton = device.findObject(By.res(PACKAGE_NAME, "buttonSettings"))
        if (settingsButton != null && settingsButton.exists()) {
            settingsButton.click()
            Thread.sleep(1000)
        }

        // Verify we're at Settings
        val settingsTitle = device.wait(
            Until.findObject(By.text("Settings")),
            TIMEOUT
        )
        assertNotNull("Should be at SettingsActivity", settingsTitle)

        checkLayoutCompliance("SettingsActivity")
    }

    @Test
    fun testProfilesActivityLayoutCompliance() {
        launchApp()

        // Navigate to Profiles
        val profilesButton = device.findObject(By.res(PACKAGE_NAME, "buttonManageProfiles"))
        if (profilesButton != null && profilesButton.exists()) {
            profilesButton.click()
            Thread.sleep(1000)
        }

        // Verify we're at Profiles activity
        val fab = device.wait(
            Until.findObject(By.res(PACKAGE_NAME, "fab")),
            TIMEOUT
        )
        assertNotNull("Should be at ProfilesActivity", fab)

        checkLayoutCompliance("ProfilesActivity")
    }

    @Test
    fun testEditProfileActivityLayoutCompliance() {
        launchApp()

        // Navigate to Profiles
        val profilesButton = device.findObject(By.res(PACKAGE_NAME, "buttonManageProfiles"))
        if (profilesButton != null && profilesButton.exists()) {
            profilesButton.click()
            Thread.sleep(1000)
        }

        // Click FAB to add new profile
        val fab = device.wait(
            Until.findObject(By.res(PACKAGE_NAME, "fab")),
            TIMEOUT
        )
        if (fab != null && fab.exists()) {
            fab.click()
            Thread.sleep(1000)
        }

        // Verify we're at EditProfile activity
        val profileNameField = device.wait(
            Until.findObject(By.res(PACKAGE_NAME, "editTextProfileName")),
            TIMEOUT
        )
        assertNotNull("Should be at EditProfileActivity", profileNameField)

        checkLayoutCompliance("EditProfileActivity")
    }

    @Test
    fun testHistoryActivityLayoutCompliance() {
        launchApp()

        // Navigate to History
        val historyButton = device.findObject(By.res(PACKAGE_NAME, "buttonHistory"))
        if (historyButton != null && historyButton.exists()) {
            historyButton.click()
            Thread.sleep(1000)
        }

        // Verify we're at History activity
        val filterCard = device.wait(
            Until.findObject(By.text("Filter History")),
            TIMEOUT
        )
        assertNotNull("Should be at HistoryActivity", filterCard)

        checkLayoutCompliance("HistoryActivity")
    }

    @Test
    fun testThemeSelectionActivityLayoutCompliance() {
        launchApp()

        // Navigate to Settings
        val settingsButton = device.findObject(By.res(PACKAGE_NAME, "buttonSettings"))
        if (settingsButton != null && settingsButton.exists()) {
            settingsButton.click()
            Thread.sleep(1000)
        }

        // Navigate to Theme selection
        val themeOption = device.findObject(By.text("Theme"))
        if (themeOption != null && themeOption.exists()) {
            themeOption.click()
            Thread.sleep(1000)
        }

        // Verify we're at Theme selection activity
        val themeItem = device.wait(
            Until.findObject(By.text("Warm Orange")),
            TIMEOUT
        )
        assertNotNull("Should be at ThemeSelectionActivity", themeItem)

        checkLayoutCompliance("ThemeSelectionActivity")
    }

    @Test
    fun testAllActivitiesHaveProperToolbars() {
        val activities = listOf(
            "MainActivity" to "buttonOpenMeTube",
            "SettingsActivity" to "Settings",
            "ProfilesActivity" to "fab",
            "HistoryActivity" to "Filter History"
        )

        launchApp()

        for ((activityName, identifier) in activities) {
            when (activityName) {
                "MainActivity" -> {
                    // Already at MainActivity
                }
                "SettingsActivity" -> {
                    val settingsButton = device.findObject(By.res(PACKAGE_NAME, "buttonSettings"))
                    settingsButton?.click()
                    Thread.sleep(1000)
                }
                "ProfilesActivity" -> {
                    device.pressBack() // Go back to main if needed
                    Thread.sleep(500)
                    val profilesButton = device.findObject(By.res(PACKAGE_NAME, "buttonManageProfiles"))
                    profilesButton?.click()
                    Thread.sleep(1000)
                }
                "HistoryActivity" -> {
                    device.pressBack() // Go back to main if needed
                    Thread.sleep(500)
                    val historyButton = device.findObject(By.res(PACKAGE_NAME, "buttonHistory"))
                    historyButton?.click()
                    Thread.sleep(1000)
                }
            }

            // Check for toolbar
            val toolbar = device.findObject(By.res(PACKAGE_NAME, "toolbar"))
            assertTrue("$activityName should have a toolbar", toolbar != null && toolbar.exists())

            // Check toolbar positioning
            if (toolbar != null) {
                val toolbarBounds = toolbar.visibleBounds
                assertTrue("$activityName: Toolbar should not overlap with status bar",
                    toolbarBounds.top > 50)
            }
        }
    }
}