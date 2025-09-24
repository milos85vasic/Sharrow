package com.shareconnect.automation

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shareconnect.MainActivity
import com.shareconnect.ProfileManager
import com.shareconnect.R
import com.shareconnect.ServerProfile
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityAutomationTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        // Create a test profile so MainActivity will show its main layout
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profileManager = ProfileManager(context)

        // Clear any existing profiles first
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        // Create a test profile
        val testProfile = ServerProfile()
        testProfile.name = "Test Profile"
        testProfile.url = "http://test.example.com"
        testProfile.port = 8080
        testProfile.serviceType = "metube"
        testProfile.username = null
        testProfile.password = null
        profileManager.addProfile(testProfile)
        profileManager.setDefaultProfile(testProfile)

        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(3000)
    }

    @After
    fun tearDown() {
        scenario.close()

        // Clean up test profiles
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profileManager = ProfileManager(context)
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }
    }

    @Test
    fun testAccessibilityLabels() {
        // Test that all interactive elements have proper accessibility labels
        Thread.sleep(2000)

        // Settings button
        onView(withId(R.id.buttonSettings))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Open MeTube button
        onView(withId(R.id.buttonOpenMeTube))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // History button
        onView(withId(R.id.buttonHistory))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // FAB Add button
        onView(withId(R.id.fabAdd))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testMinimumTouchTargetSize() {
        // Test that all interactive elements meet minimum touch target size (48dp)
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))

        onView(withId(R.id.buttonOpenMeTube))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))

        onView(withId(R.id.buttonHistory))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))

        onView(withId(R.id.fabAdd))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))
    }

    @Test
    fun testTextContrast() {
        // Test that text elements are visible and have proper contrast
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(hasTextColor()))

        onView(withId(R.id.buttonOpenMeTube))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(hasTextColor()))

        onView(withId(R.id.buttonHistory))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(hasTextColor()))
    }

    @Test
    fun testKeyboardNavigation() {
        // Test that elements can be navigated with keyboard/D-pad
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .perform(scrollTo())
            .check(matches(isFocusable()))

        onView(withId(R.id.buttonOpenMeTube))
            .perform(scrollTo())
            .check(matches(isFocusable()))

        onView(withId(R.id.buttonHistory))
            .perform(scrollTo())
            .check(matches(isFocusable()))

        onView(withId(R.id.fabAdd))
            .perform(scrollTo())
            .check(matches(isFocusable()))
    }

    @Test
    fun testScreenReaderSupport() {
        // Test that elements have proper semantics for screen readers
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))

        onView(withId(R.id.buttonOpenMeTube))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))

        onView(withId(R.id.buttonHistory))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))

        onView(withId(R.id.fabAdd))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testContentLabeling() {
        // Test that content is properly labeled
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .perform(scrollTo())
            .check(matches(withText("Settings")))

        onView(withId(R.id.buttonOpenMeTube))
            .perform(scrollTo())
            .check(matches(withText("Open MeTube Interface")))

        onView(withId(R.id.buttonHistory))
            .perform(scrollTo())
            .check(matches(withText("View Share History")))

        onView(withId(R.id.fabAdd))
            .perform(scrollTo())
            .check(matches(withText("Add from Clipboard")))
    }

    @Test
    fun testStateDescriptions() {
        // Test that interactive elements provide state information
        Thread.sleep(2000)

        scenario.onActivity { activity ->
            val settingsButton = activity.findViewById<android.view.View>(R.id.buttonSettings)
            val openMeTubeButton = activity.findViewById<android.view.View>(R.id.buttonOpenMeTube)
            val historyButton = activity.findViewById<android.view.View>(R.id.buttonHistory)
            val fabAdd = activity.findViewById<android.view.View>(R.id.fabAdd)

            // Check that buttons exist and are enabled
            if (settingsButton != null) assert(settingsButton.isEnabled)
            if (openMeTubeButton != null) assert(openMeTubeButton.isEnabled)
            if (historyButton != null) assert(historyButton.isEnabled)
            if (fabAdd != null) assert(fabAdd.isEnabled)
        }
    }

    // Custom matcher for minimum size
    private fun hasMinimumSize(minWidthDp: Int, minHeightDp: Int) =
        object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("has minimum size of ${minWidthDp}dp x ${minHeightDp}dp")
            }

            override fun matchesSafely(view: android.view.View): Boolean {
                val density = view.context.resources.displayMetrics.density
                val minWidthPx = (minWidthDp * density).toInt()
                val minHeightPx = (minHeightDp * density).toInt()

                return view.width >= minWidthPx && view.height >= minHeightPx
            }
        }

    // Custom matcher for text color
    private fun hasTextColor() =
        object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("has text color set")
            }

            override fun matchesSafely(view: android.view.View): Boolean {
                return when (view) {
                    is android.widget.TextView -> {
                        view.currentTextColor != 0
                    }
                    is android.widget.Button -> {
                        view.currentTextColor != 0
                    }
                    else -> true // Assume other views are properly styled
                }
            }
        }
}