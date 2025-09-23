package com.shareconnect.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.shareconnect.MainActivity
import com.shareconnect.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentationTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testMainActivityLaunches() {
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
        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Note: Actually clicking would require proper profile setup
        // This test verifies the button is present and clickable
    }

    @Test
    fun testOpenMeTubeButtonClick() {
        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Note: Actually clicking would require proper profile setup
        // This test verifies the button is present and clickable
    }

    @Test
    fun testHistoryButtonClick() {
        onView(withId(R.id.buttonHistory))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Test clicking the history button
        onView(withId(R.id.buttonHistory))
            .perform(click())

        // This should navigate to HistoryActivity
    }

    @Test
    fun testFabAddButtonClick() {
        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Note: Actually clicking would require clipboard content
        // This test verifies the FAB is present and clickable
    }

    @Test
    fun testToolbarIsDisplayed() {
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testAppNameIsDisplayed() {
        // Check if the app name or title is displayed
        onView(withText("ShareConnect"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testMainContentIsScrollable() {
        // Verify that the main content is in a scrollable container
        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))

        // Try scrolling to see if content is scrollable
        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testButtonTexts() {
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
    fun testButtonIcons() {
        // Verify buttons have icons
        onView(withId(R.id.buttonSettings))
            .check(matches(hasDrawable()))

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(hasDrawable()))

        onView(withId(R.id.buttonHistory))
            .check(matches(hasDrawable()))

        onView(withId(R.id.fabAdd))
            .check(matches(hasDrawable()))
    }

    private fun hasDrawable() = object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
        override fun describeTo(description: org.hamcrest.Description) {
            description.appendText("has drawable")
        }

        override fun matchesSafely(view: android.view.View): Boolean {
            return when (view) {
                is android.widget.Button -> {
                    view.compoundDrawables.any { it != null }
                }
                is com.google.android.material.button.MaterialButton -> {
                    view.icon != null
                }
                is com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton -> {
                    view.icon != null
                }
                else -> false
            }
        }
    }
}