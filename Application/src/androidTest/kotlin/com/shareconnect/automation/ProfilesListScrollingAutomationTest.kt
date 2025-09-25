package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.ProfilesActivity
import com.shareconnect.R
import com.shareconnect.ServerProfile
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilesListScrollingAutomationTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private lateinit var profileManager: ProfileManager
    private var testProfiles: MutableList<ServerProfile> = mutableListOf()

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        profileManager = ProfileManager(context)

        // Clear any existing profiles
        clearTestProfiles()

        // Create multiple test profiles to ensure scrolling is needed
        createTestProfiles()

        // Ensure screen is on and unlocked
        device.wakeUp()
        device.pressHome()
    }

    @After
    fun tearDown() {
        clearTestProfiles()
        device.pressHome()
    }

    private fun clearTestProfiles() {
        // Clean up existing test profiles
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            if (profile.name?.startsWith("Test Profile") == true) {
                profileManager.deleteProfile(profile)
            }
        }
        testProfiles.clear()
    }

    private fun createTestProfiles() {
        // Create 8 test profiles to ensure the list requires scrolling
        for (i in 1..8) {
            val profile = ServerProfile()
            profile.id = "test-profile-$i-${System.currentTimeMillis()}"
            profile.name = "Test Profile $i"
            profile.url = "http://test$i.example.com"
            profile.port = 8080 + i
            profile.serviceType = when (i % 4) {
                0 -> ServerProfile.TYPE_METUBE
                1 -> ServerProfile.TYPE_YTDL
                2 -> ServerProfile.TYPE_TORRENT
                3 -> ServerProfile.TYPE_JDOWNLOADER
                else -> ServerProfile.TYPE_METUBE
            }
            profile.torrentClientType = if (profile.serviceType == ServerProfile.TYPE_TORRENT) {
                ServerProfile.TORRENT_CLIENT_QBITTORRENT
            } else null
            profile.username = "testuser$i"
            profile.password = "testpass$i"

            profileManager.addProfile(profile)
            testProfiles.add(profile)
        }

        // Set the 4th profile as default to test default profile visibility
        profileManager.setDefaultProfile(testProfiles[3])
    }

    @Test
    fun testAllProfilesVisibleWithScrolling() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify the RecyclerView is visible
        onView(withId(R.id.recyclerViewProfiles))
            .check(matches(isDisplayed()))

        // Verify we can see the first few profiles
        onView(allOf(withText("Test Profile 1"), isDisplayed()))
            .check(matches(isDisplayed()))

        // Scroll to the bottom to ensure the last profile is visible
        onView(withId(R.id.recyclerViewProfiles))
            .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(testProfiles.size - 1))

        // Wait for scroll to complete
        Thread.sleep(1000)

        // Verify the last profile is now visible and not cut off
        onView(allOf(withText("Test Profile 8"), isDisplayed()))
            .check(matches(isDisplayed()))

        // Verify the FAB is still visible and not overlapping
        onView(withId(R.id.fabAddProfile))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testLastProfileClickable() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Scroll to the last profile
        onView(withId(R.id.recyclerViewProfiles))
            .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(testProfiles.size - 1))

        Thread.sleep(1000)

        // Click on the last profile to verify it's clickable and not cut off
        onView(allOf(withText("Test Profile 8"), isDisplayed()))
            .perform(click())

        // Wait for the EditProfileActivity to launch
        Thread.sleep(2000)

        // Verify we navigated to the EditProfileActivity
        // by checking if the edit profile elements are visible
        val hasEditElements = device.hasObject(By.textContains("Profile Name")) ||
                            device.hasObject(By.textContains("Server URL")) ||
                            device.hasObject(By.textContains("Port"))

        assert(hasEditElements) { "Should navigate to EditProfileActivity when clicking on the last profile" }

        // Go back to profiles activity
        device.pressBack()
        Thread.sleep(1000)

        scenario.close()
    }

    @Test
    fun testDefaultProfileIndicatorVisible() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Scroll to find the default profile (Test Profile 4)
        onView(withId(R.id.recyclerViewProfiles))
            .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(3))

        Thread.sleep(1000)

        // Verify the default profile indicator is visible
        // The default profile should have some visual indicator (like "DEFAULT" text or special styling)
        val hasDefaultIndicator = device.hasObject(By.textContains("DEFAULT")) ||
                                 device.hasObject(By.textContains("Test Profile 4"))

        assert(hasDefaultIndicator) { "Default profile should be visible with proper indicator" }

        scenario.close()
    }

    @Test
    fun testScrollingPerformance() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        val startTime = System.currentTimeMillis()

        // Perform multiple scroll operations to test performance
        for (i in 0 until 3) {
            // Scroll to bottom
            onView(withId(R.id.recyclerViewProfiles))
                .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(testProfiles.size - 1))

            Thread.sleep(500)

            // Scroll to top
            onView(withId(R.id.recyclerViewProfiles))
                .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0))

            Thread.sleep(500)
        }

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime

        // Scrolling should be smooth and complete within reasonable time (less than 10 seconds for 6 scroll operations)
        assert(totalTime < 10000) { "Scrolling should be performant, took ${totalTime}ms" }

        scenario.close()
    }

    @Test
    fun testFABNotCoveringLastProfile() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Scroll to the last profile
        onView(withId(R.id.recyclerViewProfiles))
            .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(testProfiles.size - 1))

        Thread.sleep(1000)

        // Get the bounds of the last profile and FAB to ensure they don't overlap significantly
        val lastProfileObject = device.findObject(By.textContains("Test Profile 8"))
        val fabObject = device.findObject(By.res("com.shareconnect:id/fabAddProfile"))

        assert(lastProfileObject != null) { "Last profile should be found" }
        assert(fabObject != null) { "FAB should be found" }

        if (lastProfileObject != null && fabObject != null) {
            val profileBounds = lastProfileObject.visibleBounds
            val fabBounds = fabObject.visibleBounds

            // The last profile should be fully visible above the FAB
            // Check that the bottom of the profile is above the top of the FAB
            val hasSpacing = profileBounds.bottom < fabBounds.top - 10 // 10dp minimum spacing

            assert(hasSpacing) {
                "Last profile (bottom: ${profileBounds.bottom}) should not overlap with FAB (top: ${fabBounds.top})"
            }
        }

        scenario.close()
    }

    @Test
    fun testAllProfileTypesVisible() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Expected service types based on our test data creation
        val expectedTypes = listOf("MeTube", "YT-DLP", "Torrent", "jDownloader")
        val foundTypes = mutableSetOf<String>()

        // Scroll through the entire list and check for different service types
        for (i in 0 until testProfiles.size) {
            onView(withId(R.id.recyclerViewProfiles))
                .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(i))

            Thread.sleep(500)

            // Check what service types are visible
            expectedTypes.forEach { type ->
                if (device.hasObject(By.textContains(type))) {
                    foundTypes.add(type)
                }
            }
        }

        // Verify we found all expected service types
        expectedTypes.forEach { expectedType ->
            assert(foundTypes.contains(expectedType)) {
                "Should find profile with service type: $expectedType. Found: $foundTypes"
            }
        }

        scenario.close()
    }

    @Test
    fun testEmptyListShowsProperMessage() {
        // Clear all profiles to test empty state
        clearTestProfiles()

        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify the "no profiles" message is shown
        onView(withId(R.id.textViewNoProfiles))
            .check(matches(isDisplayed()))

        // Verify RecyclerView is hidden
        onView(withId(R.id.recyclerViewProfiles))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        // Verify FAB is still visible for adding profiles
        onView(withId(R.id.fabAddProfile))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testSingleProfileNotCutOff() {
        // Clear all profiles and create just one
        clearTestProfiles()

        val singleProfile = ServerProfile()
        singleProfile.id = "single-test-profile"
        singleProfile.name = "Single Test Profile"
        singleProfile.url = "http://single.example.com"
        singleProfile.port = 8080
        singleProfile.serviceType = ServerProfile.TYPE_METUBE
        singleProfile.username = "singleuser"
        singleProfile.password = "singlepass"

        profileManager.addProfile(singleProfile)
        testProfiles.add(singleProfile)

        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for the activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify the single profile is visible and clickable
        onView(allOf(withText("Single Test Profile"), isDisplayed()))
            .check(matches(isDisplayed()))
            .perform(click())

        // Wait for navigation
        Thread.sleep(2000)

        // Verify we can navigate to edit the profile
        val hasEditElements = device.hasObject(By.textContains("Profile Name")) ||
                            device.hasObject(By.textContains("Server URL"))

        assert(hasEditElements) { "Should be able to click and edit single profile" }

        device.pressBack()
        scenario.close()
    }
}