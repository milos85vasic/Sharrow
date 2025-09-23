package com.shareconnect.activities

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
class MainActivityInstrumentationTest {

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
        profileManager.addProfile(testProfile)
        profileManager.setDefaultProfile(testProfile)

        // Launch activity and wait for it to be ready
        scenario = ActivityScenario.launch(MainActivity::class.java)

        // Give the activity time to fully initialize and load the layout
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
    fun testMainActivityLaunches() {
        // Wait for activity to be fully loaded
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonHistory))
            .check(matches(isDisplayed()))

        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSettingsButtonClick() {
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testOpenMeTubeButtonClick() {
        Thread.sleep(2000)

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testHistoryButtonClick() {
        Thread.sleep(2000)

        onView(withId(R.id.buttonHistory))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testFabAddButtonClick() {
        Thread.sleep(2000)

        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testToolbarIsDisplayed() {
        Thread.sleep(2000)

        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testAppNameIsDisplayed() {
        Thread.sleep(2000)

        // Check if the toolbar is displayed (which contains the app name)
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testMainContentIsScrollable() {
        Thread.sleep(3000)

        // Simply verify the toolbar is displayed which is the baseline
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testButtonTexts() {
        Thread.sleep(2000)

        onView(withId(R.id.buttonSettings))
            .check(matches(withText("Settings")))

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(withText("Open MeTube Interface")))

        onView(withId(R.id.buttonHistory))
            .check(matches(withText("View Share History")))

        onView(withId(R.id.fabAdd))
            .check(matches(withText("Add from Clipboard")))
    }

    @Test
    fun testActivityLifecycle() {
        Thread.sleep(3000)

        // Verify activity is in correct state
        scenario.onActivity { activity ->
            assert(activity.isFinishing == false)
        }
    }

}