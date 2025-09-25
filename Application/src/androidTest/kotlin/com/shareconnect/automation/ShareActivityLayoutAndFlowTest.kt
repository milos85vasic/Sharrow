package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.R
import com.shareconnect.ServerProfile
import com.shareconnect.ShareActivity
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareActivityLayoutAndFlowTest {

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
        val existingProfiles = profileManager.profiles
        for (profile in existingProfiles) {
            profileManager.deleteProfile(profile)
        }

        // Ensure screen is on and unlocked
        device.wakeUp()
        device.pressHome()
    }

    @After
    fun tearDown() {
        // Clean up test profiles
        for (profile in testProfiles) {
            profileManager.deleteProfile(profile)
        }
        testProfiles.clear()

        device.pressHome()
    }

    @Test
    fun testShareActivityLayoutProperlyFitsScreen() {
        // Create a test profile
        val testProfile = createProfile("Test MeTube", ServerProfile.TYPE_METUBE)

        // Start ShareActivity with a test URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify the main components are visible and properly positioned
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("youtube.com"))))

        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonSendToMeTube))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonShareToApps))
            .check(matches(isDisplayed()))

        // Verify that all components are within screen bounds by checking they are completely displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.autoCompleteProfiles))
            .check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.buttonSendToMeTube))
            .check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.buttonShareToApps))
            .check(matches(isCompletelyDisplayed()))

        scenario.close()
    }

    @Test
    fun testScrollingWorksProperlyInShareActivity() {
        // Create a test profile
        val testProfile = createProfile("Test MeTube", ServerProfile.TYPE_METUBE)

        // Start ShareActivity
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=longTestVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(1000)

        // Test that we can scroll to see all content
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))

        // Scroll down to ensure bottom buttons are accessible
        onView(withId(R.id.buttonShareToApps))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Scroll back up to ensure top content is still accessible
        onView(withId(R.id.textViewYouTubeLink))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testCompleteShareToServiceFlowWithDismissal() {
        // Create a test MeTube profile
        val testProfile = createProfile("Test MeTube Server", ServerProfile.TYPE_METUBE)

        // Start ShareActivity with a streaming URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify ShareActivity is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))

        // Select the test profile
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(500)

        onView(withText(containsString("Test MeTube Server")))
            .perform(click())

        // Click the send button
        onView(withId(R.id.buttonSendToMeTube))
            .perform(click())

        // Wait for the operation to complete and activity to dismiss
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify activity is dismissed by checking that we're no longer in ShareActivity
        // We should not be able to find the ShareActivity components anymore
        val shareActivityExists = try {
            device.findObject(UiSelector().resourceId("com.shareconnect:id/textViewYouTubeLink"))
                .waitForExists(1000)
        } catch (e: Exception) {
            false
        }

        // ShareActivity should be dismissed (components should not exist anymore)
        assert(!shareActivityExists) { "ShareActivity should be dismissed after successful sharing" }

        scenario.close()
    }

    @Test
    fun testCompleteShareToAppsFlowWithDismissal() {
        // Create a test profile
        val testProfile = createProfile("Test Profile", ServerProfile.TYPE_METUBE)

        // Start ShareActivity
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(1000)

        // Click share to apps button
        onView(withId(R.id.buttonShareToApps))
            .perform(click())

        // Wait for share chooser to appear
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify share chooser appeared by looking for typical system share dialog
        val shareDialogExists = device.findObject(
            UiSelector().textContains("Share")
        ).waitForExists(2000)

        assert(shareDialogExists) { "Share dialog should appear" }

        // Press back to cancel the share dialog
        device.pressBack()
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify ShareActivity is dismissed after attempting to share to apps
        val shareActivityExists = try {
            device.findObject(UiSelector().resourceId("com.shareconnect:id/textViewYouTubeLink"))
                .waitForExists(1000)
        } catch (e: Exception) {
            false
        }

        // ShareActivity should be dismissed
        assert(!shareActivityExists) { "ShareActivity should be dismissed after share to apps" }

        scenario.close()
    }

    @Test
    fun testWebUIFlowWithDismissal() {
        // Create a test torrent profile
        val testProfile = createProfile("Test qBittorrent", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)

        // Start ShareActivity with a torrent URL
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "magnet:?xt=urn:btih:testmagnetlink123456789")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(1000)

        // Select the torrent profile
        onView(withId(R.id.autoCompleteProfiles)).perform(click())
        device.waitForIdle()
        Thread.sleep(500)

        onView(withText(containsString("Test qBittorrent")))
            .perform(click())

        // Click the send button
        onView(withId(R.id.buttonSendToMeTube))
            .perform(click())

        // Wait for WebUI to launch and ShareActivity to dismiss
        device.waitForIdle()
        Thread.sleep(2000)

        // Verify ShareActivity is dismissed
        val shareActivityExists = try {
            device.findObject(UiSelector().resourceId("com.shareconnect:id/textViewYouTubeLink"))
                .waitForExists(1000)
        } catch (e: Exception) {
            false
        }

        // ShareActivity should be dismissed after launching WebUI
        assert(!shareActivityExists) { "ShareActivity should be dismissed after launching WebUI" }

        scenario.close()
    }

    @Test
    fun testLayoutResponsivenessOnDifferentOrientations() {
        // Create a test profile
        val testProfile = createProfile("Test MeTube", ServerProfile.TYPE_METUBE)

        // Start ShareActivity
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=testVideo123")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<ShareActivity>(intent)

        // Wait for activity to load
        device.waitForIdle()
        Thread.sleep(1000)

        // Test portrait orientation
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.buttonShareToApps))
            .perform(scrollTo())
            .check(matches(isCompletelyDisplayed()))

        // Rotate to landscape
        device.setOrientationLeft()
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify components are still properly displayed in landscape
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))
        onView(withId(R.id.buttonShareToApps))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Rotate back to portrait
        device.setOrientationNatural()
        device.waitForIdle()
        Thread.sleep(1000)

        // Verify components are still properly displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    private fun createProfile(
        name: String,
        serviceType: String,
        torrentClientType: String? = null
    ): ServerProfile {
        val profile = ServerProfile()
        profile.id = "test-$name-${System.currentTimeMillis()}"
        profile.name = name
        profile.url = "http://localhost"
        profile.port = 8080
        profile.serviceType = serviceType
        profile.torrentClientType = torrentClientType
        profile.username = "testuser"
        profile.password = "testpass"

        profileManager.addProfile(profile)
        testProfiles.add(profile)
        return profile
    }
}