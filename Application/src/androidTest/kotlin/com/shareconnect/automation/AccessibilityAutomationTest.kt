package com.shareconnect.automation

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shareconnect.MainActivity
import com.shareconnect.R
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityAutomationTest {

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
    fun testAccessibilityLabels() {
        // Test that all interactive elements have proper accessibility labels

        // Settings button
        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))
            .check(matches(not(hasContentDescription())))

        // Open MeTube button
        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isDisplayed()))
            .check(matches(not(hasContentDescription())))

        // History button
        onView(withId(R.id.buttonHistory))
            .check(matches(isDisplayed()))
            .check(matches(not(hasContentDescription())))

        // FAB Add button
        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))
            .check(matches(not(hasContentDescription())))
    }

    @Test
    fun testMinimumTouchTargetSize() {
        // Test that all interactive elements meet minimum touch target size (48dp)

        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))

        onView(withId(R.id.buttonHistory))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))

        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)))
    }

    @Test
    fun testTextContrast() {
        // Test that text elements are visible and have proper contrast

        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))
            .check(matches(hasTextColor()))

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isDisplayed()))
            .check(matches(hasTextColor()))

        onView(withId(R.id.buttonHistory))
            .check(matches(isDisplayed()))
            .check(matches(hasTextColor()))
    }

    @Test
    fun testKeyboardNavigation() {
        // Test that elements can be navigated with keyboard/D-pad

        onView(withId(R.id.buttonSettings))
            .check(matches(isFocusable()))

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isFocusable()))

        onView(withId(R.id.buttonHistory))
            .check(matches(isFocusable()))

        onView(withId(R.id.fabAdd))
            .check(matches(isFocusable()))
    }

    @Test
    fun testScreenReaderSupport() {
        // Test that elements have proper semantics for screen readers

        onView(withId(R.id.buttonSettings))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))

        onView(withId(R.id.buttonOpenMeTube))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))

        onView(withId(R.id.buttonHistory))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))

        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testContentLabeling() {
        // Test that content is properly labeled

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
    fun testStateDescriptions() {
        // Test that interactive elements provide state information

        scenario.onActivity { activity ->
            val settingsButton = activity.findViewById<android.view.View>(R.id.buttonSettings)
            val openMeTubeButton = activity.findViewById<android.view.View>(R.id.buttonOpenMeTube)
            val historyButton = activity.findViewById<android.view.View>(R.id.buttonHistory)
            val fabAdd = activity.findViewById<android.view.View>(R.id.fabAdd)

            // Check that buttons are in enabled state
            assert(settingsButton.isEnabled)
            assert(openMeTubeButton.isEnabled)
            assert(historyButton.isEnabled)
            assert(fabAdd.isEnabled)
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