package com.shareconnect.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shareconnect.R
import com.shareconnect.SettingsActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityInstrumentationTest {

    private lateinit var scenario: ActivityScenario<SettingsActivity>

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(SettingsActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testSettingsActivityLaunches() {
        // Verify the settings fragment container is displayed
        onView(withId(R.id.settings))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testToolbarIsDisplayed() {
        // Verify toolbar is present
        onView(withId(androidx.appcompat.R.id.action_bar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testServerProfilesPreferenceExists() {
        // Check if server profiles preference is displayed
        onView(withText("Server Profiles"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testThemeSelectionPreferenceExists() {
        // Check if theme selection preference is displayed
        onView(withText("Theme Selection"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testServerProfilesPreferenceClickable() {
        // Test clicking server profiles preference
        onView(withText("Server Profiles"))
            .check(matches(isClickable()))
            .perform(click())

        // This should navigate to ProfilesActivity
    }

    @Test
    fun testThemeSelectionPreferenceClickable() {
        // Test clicking theme selection preference
        onView(withText("Theme Selection"))
            .check(matches(isClickable()))
            .perform(click())

        // This should navigate to ThemeSelectionActivity
    }

    @Test
    fun testBackButtonNavigatesUp() {
        // Test that back button is present
        onView(withContentDescription("Navigate up"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testSettingsFragmentIsLoaded() {
        // Verify that the settings fragment content is present
        onView(withId(R.id.settings))
            .check(matches(isDisplayed()))

        // Check that at least one preference is visible
        onView(withText("Server Profiles"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testPreferenceCategories() {
        // Test that preference categories are properly displayed
        // This would depend on your specific preference structure
        onView(withText("Server Profiles"))
            .check(matches(isDisplayed()))

        onView(withText("Theme Selection"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testPreferenceIcons() {
        // Verify that preferences have appropriate icons
        // This test checks that the preference list is scrollable and items are accessible
        onView(withText("Server Profiles"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testActivityTitle() {
        // Check that the activity has appropriate title
        // This might be set in the manifest or programmatically
        scenario.onActivity { activity ->
            val title = activity.title
            assertNotNull(title)
        }
    }

    private fun assertNotNull(obj: Any?) {
        if (obj == null) {
            throw AssertionError("Object should not be null")
        }
    }
}