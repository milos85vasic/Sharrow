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
import org.junit.Assert.fail

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
        // Get screen dimensions in pixels
        val displayMetrics = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        println("🔍 Checking layout compliance for $activityName")
        println("   Screen size: ${screenWidth}x${screenHeight}")

        // Find the main content area
        val mainContent = device.findObject(By.pkg(PACKAGE_NAME))
        if (mainContent == null) {
            fail("No main content found for $activityName")
            return
        }

        val contentBounds = mainContent.visibleBounds
        println("   Content bounds: ${contentBounds}")

        // More strict status bar overlap check
        // Status bar is typically 24-48dp (72-144px at 3x density)
        val statusBarHeight = 144 // Conservative estimate for high-density displays
        if (contentBounds.top < statusBarHeight) {
            fail("$activityName: Content overlapping with status bar. Top: ${contentBounds.top}, Expected minimum: $statusBarHeight")
        }

        // Check for toolbar presence and positioning
        val toolbar = device.findObject(By.res(PACKAGE_NAME, "toolbar"))
        if (toolbar != null) {
            val toolbarBounds = toolbar.visibleBounds
            println("   Toolbar bounds: ${toolbarBounds}")

            // Toolbar should be positioned below status bar
            if (toolbarBounds.top < statusBarHeight) {
                fail("$activityName: Toolbar overlapping with status bar. Top: ${toolbarBounds.top}, Expected minimum: $statusBarHeight")
            }

            // Toolbar should have reasonable height (typically 56dp = 168px at 3x)
            val toolbarHeight = toolbarBounds.bottom - toolbarBounds.top
            if (toolbarHeight < 120 || toolbarHeight > 200) {
                fail("$activityName: Toolbar has unusual height: ${toolbarHeight}px. Expected: 120-200px")
            }
        } else {
            println("   ⚠️  No toolbar found in $activityName")
        }

        // Check content positioning relative to toolbar
        if (toolbar != null) {
            val toolbarBounds = toolbar.visibleBounds
            val contentArea = device.findObject(By.clazz("android.widget.ScrollView")
                .clazz("androidx.recyclerview.widget.RecyclerView")
                .clazz("android.widget.LinearLayout"))

            if (contentArea != null) {
                val contentAreaBounds = contentArea.visibleBounds
                if (contentAreaBounds.top <= toolbarBounds.bottom + 10) {
                    fail("$activityName: Content area overlapping with toolbar. Content top: ${contentAreaBounds.top}, Toolbar bottom: ${toolbarBounds.bottom}")
                }
            }
        }

        // Check for navigation bar overlap (bottom)
        val navigationBarHeight = 126 // Conservative estimate (42dp at 3x density)
        val maxContentBottom = screenHeight - navigationBarHeight

        val scrollView = device.findObject(By.clazz("android.widget.ScrollView"))
        if (scrollView != null) {
            val scrollBounds = scrollView.visibleBounds
            println("   ScrollView bounds: ${scrollBounds}")

            if (scrollBounds.bottom > maxContentBottom) {
                fail("$activityName: ScrollView overlapping with navigation bar. Bottom: ${scrollBounds.bottom}, Max allowed: $maxContentBottom")
            }
        }

        val recyclerView = device.findObject(By.clazz("androidx.recyclerview.widget.RecyclerView"))
        if (recyclerView != null) {
            val recyclerBounds = recyclerView.visibleBounds
            println("   RecyclerView bounds: ${recyclerBounds}")

            if (recyclerBounds.bottom > maxContentBottom) {
                fail("$activityName: RecyclerView overlapping with navigation bar. Bottom: ${recyclerBounds.bottom}, Max allowed: $maxContentBottom")
            }
        }

        // Check for FloatingActionButton positioning
        val fab = device.findObject(By.clazz("com.google.android.material.floatingactionbutton.FloatingActionButton"))
        if (fab != null) {
            val fabBounds = fab.visibleBounds
            println("   FAB bounds: ${fabBounds}")

            if (fabBounds.bottom > maxContentBottom) {
                fail("$activityName: FloatingActionButton overlapping with navigation bar. Bottom: ${fabBounds.bottom}, Max allowed: $maxContentBottom")
            }
        }

        // Verify fitsSystemWindows is working by checking reasonable margins
        val reasonableTopMargin = statusBarHeight - 20 // Allow some tolerance
        val reasonableBottomMargin = screenHeight - navigationBarHeight + 20

        if (contentBounds.top < reasonableTopMargin) {
            fail("$activityName: Content too close to status bar, fitsSystemWindows may not be working properly")
        }

        if (contentBounds.bottom > reasonableBottomMargin) {
            fail("$activityName: Content too close to navigation bar, fitsSystemWindows may not be working properly")
        }

        println("✅ $activityName: Layout compliance check PASSED")
        println("   Status bar clearance: ${contentBounds.top}px")
        println("   Navigation bar clearance: ${screenHeight - contentBounds.bottom}px")
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
        if (settingsButton != null) {
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
        if (profilesButton != null) {
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
        if (profilesButton != null) {
            profilesButton.click()
            Thread.sleep(1000)
        }

        // Click FAB to add new profile
        val fab = device.wait(
            Until.findObject(By.res(PACKAGE_NAME, "fab")),
            TIMEOUT
        )
        if (fab != null) {
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
        if (historyButton != null) {
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
        if (settingsButton != null) {
            settingsButton.click()
            Thread.sleep(1000)
        }

        // Navigate to Theme selection
        val themeOption = device.findObject(By.text("Theme"))
        if (themeOption != null) {
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
            assertTrue("$activityName should have a toolbar", toolbar != null)

            // Check toolbar positioning
            if (toolbar != null) {
                val toolbarBounds = toolbar.visibleBounds
                assertTrue("$activityName: Toolbar should not overlap with status bar",
                    toolbarBounds.top > 50)
            }
        }
    }
}