package com.shareconnect.automation

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.ProfilesActivity
import com.shareconnect.R
import com.shareconnect.ServerProfile
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilesListCutOffTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private lateinit var profileManager: ProfileManager
    private var testProfiles: MutableList<ServerProfile> = mutableListOf()

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        profileManager = ProfileManager(context)

        // Clear any existing profiles first
        clearTestData()

        // Create enough profiles to require scrolling (6 profiles should be enough)
        createTestProfiles(6)

        // Ensure screen is on and unlocked
        device.wakeUp()
        device.pressHome()
    }

    @After
    fun tearDown() {
        clearTestData()
        device.pressHome()
    }

    private fun clearTestData() {
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            if (profile.name?.startsWith("ScrollTest") == true) {
                profileManager.deleteProfile(profile)
            }
        }
        testProfiles.clear()
    }

    private fun createTestProfiles(count: Int) {
        for (i in 1..count) {
            val profile = ServerProfile()
            profile.id = "scroll-test-$i-${System.currentTimeMillis()}"
            profile.name = "ScrollTest Profile $i"
            profile.url = "http://test$i.example.com"
            profile.port = 8080 + i
            profile.serviceType = ServerProfile.TYPE_METUBE
            profile.username = "testuser$i"
            profile.password = "testpass$i"

            profileManager.addProfile(profile)
            testProfiles.add(profile)
        }
    }

    @Test
    fun testLastProfileNotCutOffByFAB() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Scroll to the last profile
        onView(withId(R.id.recyclerViewProfiles))
            .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(testProfiles.size - 1))

        // Wait for scroll animation to complete
        Thread.sleep(1500)

        // Find the last profile and FAB objects
        val lastProfileText = device.findObject(By.textContains("ScrollTest Profile 6"))
        val fabObject = device.findObject(By.res("com.shareconnect:id/fabAddProfile"))

        // Verify both elements exist
        assert(lastProfileText != null) { "Last profile should be found on screen" }
        assert(fabObject != null) { "FAB should be found on screen" }

        if (lastProfileText != null && fabObject != null) {
            val profileBounds = lastProfileText.visibleBounds
            val fabBounds = fabObject.visibleBounds

            println("Profile bounds: $profileBounds")
            println("FAB bounds: $fabBounds")

            // The profile should be completely above the FAB with some spacing
            val isProfileAboveFAB = profileBounds.bottom <= fabBounds.top

            // Allow for some small overlap (maybe 10px) but profile should be mostly visible
            val hasMinimalOverlap = profileBounds.bottom <= fabBounds.top + 10

            assert(isProfileAboveFAB || hasMinimalOverlap) {
                "Last profile should not be significantly cut off by FAB. " +
                "Profile bottom: ${profileBounds.bottom}, FAB top: ${fabBounds.top}"
            }
        }

        scenario.close()
    }

    @Test
    fun testProfilesListScrollableWithMultipleProfiles() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Check that the first profile is visible initially
        val firstProfile = device.findObject(By.textContains("ScrollTest Profile 1"))
        assert(firstProfile != null) { "First profile should be visible initially" }

        // Scroll to the last profile
        onView(withId(R.id.recyclerViewProfiles))
            .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(testProfiles.size - 1))

        Thread.sleep(1500)

        // Check that the last profile is now visible
        val lastProfile = device.findObject(By.textContains("ScrollTest Profile 6"))
        assert(lastProfile != null) { "Last profile should be visible after scrolling" }

        // Scroll back to top
        onView(withId(R.id.recyclerViewProfiles))
            .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0))

        Thread.sleep(1500)

        // Check that first profile is visible again
        val firstProfileAgain = device.findObject(By.textContains("ScrollTest Profile 1"))
        assert(firstProfileAgain != null) { "First profile should be visible again after scrolling back" }

        scenario.close()
    }

    @Test
    fun testFABRemainsVisibleDuringScroll() {
        val scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(2000)

        // Check FAB is initially visible
        val fabInitial = device.findObject(By.res("com.shareconnect:id/fabAddProfile"))
        assert(fabInitial != null) { "FAB should be visible initially" }

        // Scroll to different positions and ensure FAB remains visible
        for (i in 0 until testProfiles.size) {
            onView(withId(R.id.recyclerViewProfiles))
                .perform(RecyclerViewActions.scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(i))

            Thread.sleep(500)

            val fab = device.findObject(By.res("com.shareconnect:id/fabAddProfile"))
            assert(fab != null) { "FAB should remain visible at position $i" }
        }

        scenario.close()
    }
}