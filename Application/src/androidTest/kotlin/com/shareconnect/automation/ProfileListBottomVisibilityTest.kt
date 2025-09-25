package com.shareconnect.automation

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.ProfilesActivity
import com.shareconnect.ServerProfile
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileListBottomVisibilityTest {

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<ProfilesActivity>
    private lateinit var profileManager: ProfileManager

    companion object {
        private const val TIMEOUT = 5000L
        private const val SHORT_TIMEOUT = 2000L
    }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val context = ApplicationProvider.getApplicationContext<Context>()
        profileManager = ProfileManager(context)

        // Create multiple test profiles to ensure we have enough items for scrolling
        createTestProfiles()

        // Launch ProfilesActivity
        scenario = ActivityScenario.launch(ProfilesActivity::class.java)

        // Wait for activity to be fully loaded
        device.waitForIdle()
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        // Clean up test profiles
        cleanupTestProfiles()

        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    private fun createTestProfiles() {
        // Clear existing profiles first
        cleanupTestProfiles()

        // Create enough test profiles to make the list scrollable (at least 6-8 profiles)
        val testProfiles = mutableListOf<ServerProfile>()

        for (i in 1..8) {
            val profile = ServerProfile().apply {
                id = "test-profile-$i"
                name = "Test Profile $i"
                url = "http://test$i.example.com"
                port = 8080 + i
                serviceType = when (i % 3) {
                    0 -> ServerProfile.TYPE_METUBE
                    1 -> ServerProfile.TYPE_TORRENT
                    else -> ServerProfile.TYPE_JDOWNLOADER
                }
                username = if (i % 2 == 0) "user$i" else null
                password = if (i % 2 == 0) "pass$i" else null
                isDefault = (i == 1)
            }
            testProfiles.add(profile)
        }

        // Add all test profiles
        testProfiles.forEach { profile ->
            profileManager.addProfile(profile)
        }
    }

    private fun cleanupTestProfiles() {
        profileManager.profiles.filter { it.id?.startsWith("test-profile-") == true }.forEach { profile ->
            profileManager.deleteProfile(profile)
        }
    }

    @Test
    fun testLastItemFullyVisible() {
        // Verify ProfilesActivity is displayed
        val toolbar = device.findObject(UiSelector().text("Server Profiles"))
        Assert.assertTrue("ProfilesActivity should be displayed", toolbar.exists())

        // Find the RecyclerView
        val recyclerView = device.findObject(UiSelector().resourceId("com.shareconnect:id/recyclerViewProfiles"))
        Assert.assertTrue("RecyclerView should be present", recyclerView.exists())

        // Wait for profiles to load
        Thread.sleep(SHORT_TIMEOUT)

        // Scroll to the bottom to find the last item
        scrollToBottom(recyclerView)

        // Get the last visible profile item
        val lastProfileItem = findLastProfileItem()
        Assert.assertNotNull("Last profile item should be found", lastProfileItem)

        // Verify the last item is fully visible
        verifyItemFullyVisible(lastProfileItem!!)
    }

    @Test
    fun testFABDoesNotOverlapLastItem() {
        // Find the FAB
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAddProfile"))
        Assert.assertTrue("FAB should be present", fab.exists())

        // Scroll to bottom
        val recyclerView = device.findObject(UiSelector().resourceId("com.shareconnect:id/recyclerViewProfiles"))
        scrollToBottom(recyclerView)

        // Get FAB bounds
        val fabBounds = fab.getBounds()

        // Get last item bounds
        val lastItem = findLastProfileItem()
        Assert.assertNotNull("Last item should be found", lastItem)

        val lastItemBounds = lastItem!!.getBounds()

        // Verify FAB doesn't overlap with last item (there should be sufficient space)
        val verticalGap = fabBounds.top - lastItemBounds.bottom
        val displayMetrics = ApplicationProvider.getApplicationContext<Context>().resources.displayMetrics
        val densityDpi = displayMetrics.densityDpi
        val minGapPx = (16 * densityDpi) / 160 // Convert 16dp to pixels

        Assert.assertTrue("FAB should not overlap last item - gap should be at least 16dp (~${minGapPx}px), actual gap: ${verticalGap}px",
            verticalGap >= minGapPx)
    }

    @Test
    fun testAllItemButtonsAccessible() {
        // Scroll to bottom
        val recyclerView = device.findObject(UiSelector().resourceId("com.shareconnect:id/recyclerViewProfiles"))
        scrollToBottom(recyclerView)

        // Find the last item
        val lastItem = findLastProfileItem()
        Assert.assertNotNull("Last item should be found", lastItem)

        // Check that buttons in the last item are fully visible and clickable
        val setDefaultButton = device.findObject(
            UiSelector().resourceId("com.shareconnect:id/buttonSetDefault")
                .instance(getLastItemIndex())
        )

        val deleteButton = device.findObject(
            UiSelector().resourceId("com.shareconnect:id/buttonDelete")
                .instance(getLastItemIndex())
        )

        Assert.assertTrue("Set Default button should be visible in last item", setDefaultButton.exists())
        Assert.assertTrue("Delete button should be visible in last item", deleteButton.exists())

        // Verify buttons are fully within screen bounds
        val screenHeight = device.displayHeight
        val setDefaultBounds = setDefaultButton.getBounds()
        val deleteBounds = deleteButton.getBounds()

        Assert.assertTrue("Set Default button should be fully visible on screen",
            setDefaultBounds.bottom < screenHeight - 50) // 50px buffer for system UI

        Assert.assertTrue("Delete button should be fully visible on screen",
            deleteBounds.bottom < screenHeight - 50)
    }

    @Test
    fun testScrollingBehaviorWithFAB() {
        val recyclerView = device.findObject(UiSelector().resourceId("com.shareconnect:id/recyclerViewProfiles"))

        // Start from top
        scrollToTop(recyclerView)
        Thread.sleep(500)

        // Get initial first item position
        val firstItemInitial = device.findObject(
            UiSelector().resourceId("com.shareconnect:id/textViewProfileName")
                .instance(0)
        )

        val initialTop = if (firstItemInitial.exists()) firstItemInitial.getBounds().top else 0

        // Scroll to bottom
        scrollToBottom(recyclerView)
        Thread.sleep(500)

        // Verify we can scroll back up
        scrollToTop(recyclerView)
        Thread.sleep(500)

        // Verify first item is back at original position (or close to it)
        val firstItemFinal = device.findObject(
            UiSelector().resourceId("com.shareconnect:id/textViewProfileName")
                .instance(0)
        )

        if (firstItemFinal.exists()) {
            val finalTop = firstItemFinal.getBounds().top
            val difference = Math.abs(finalTop - initialTop)
            Assert.assertTrue("Should be able to scroll back to original position, difference: ${difference}px",
                difference < 100) // Allow some tolerance
        }
    }

    @Test
    fun testItemContentFullyVisible() {
        // Scroll to bottom
        val recyclerView = device.findObject(UiSelector().resourceId("com.shareconnect:id/recyclerViewProfiles"))
        scrollToBottom(recyclerView)

        // Get all text elements in the last item to verify they're visible
        val lastItemIndex = getLastItemIndex()

        val profileName = device.findObject(
            UiSelector().resourceId("com.shareconnect:id/textViewProfileName")
                .instance(lastItemIndex)
        )

        val profileUrl = device.findObject(
            UiSelector().resourceId("com.shareconnect:id/textViewProfileUrl")
                .instance(lastItemIndex)
        )

        val serviceType = device.findObject(
            UiSelector().resourceId("com.shareconnect:id/textViewServiceType")
                .instance(lastItemIndex)
        )

        // Verify all text elements are visible
        Assert.assertTrue("Profile name should be visible in last item", profileName.exists())
        Assert.assertTrue("Profile URL should be visible in last item", profileUrl.exists())
        Assert.assertTrue("Service type should be visible in last item", serviceType.exists())

        // Verify text is not empty (indicating it loaded properly)
        Assert.assertTrue("Profile name should not be empty", profileName.text.isNotEmpty())
        Assert.assertTrue("Profile URL should not be empty", profileUrl.text.isNotEmpty())
        Assert.assertTrue("Service type should not be empty", serviceType.text.isNotEmpty())
    }

    private fun scrollToBottom(recyclerView: UiObject) {
        // Scroll down multiple times to ensure we reach the bottom
        repeat(10) {
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight * 3 / 4,
                device.displayWidth / 2,
                device.displayHeight / 4,
                10
            )
            device.waitForIdle()
            Thread.sleep(300)
        }
    }

    private fun scrollToTop(recyclerView: UiObject) {
        // Scroll up multiple times to ensure we reach the top
        repeat(10) {
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight / 4,
                device.displayWidth / 2,
                device.displayHeight * 3 / 4,
                10
            )
            device.waitForIdle()
            Thread.sleep(300)
        }
    }

    private fun findLastProfileItem(): UiObject? {
        // Find all profile name TextViews and get the last visible one
        var lastItem: UiObject? = null
        var index = 0

        while (true) {
            val item = device.findObject(
                UiSelector().resourceId("com.shareconnect:id/textViewProfileName")
                    .instance(index)
            )

            if (!item.exists()) {
                break
            }

            lastItem = item
            index++
        }

        return lastItem
    }

    private fun getLastItemIndex(): Int {
        var lastIndex = 0
        var index = 0

        while (true) {
            val item = device.findObject(
                UiSelector().resourceId("com.shareconnect:id/textViewProfileName")
                    .instance(index)
            )

            if (!item.exists()) {
                break
            }

            lastIndex = index
            index++
        }

        return lastIndex
    }

    private fun verifyItemFullyVisible(item: UiObject) {
        val itemBounds = item.getBounds()
        val screenHeight = device.displayHeight
        val screenWidth = device.displayWidth

        // Check if item is within screen bounds with some buffer for system UI
        Assert.assertTrue("Item should be horizontally within screen bounds",
            itemBounds.left >= 0 && itemBounds.right <= screenWidth)

        Assert.assertTrue("Item should be vertically within screen bounds",
            itemBounds.top >= 0 && itemBounds.bottom <= screenHeight - 100) // 100px buffer for system UI and potential overlaps

        // Additional check: item should have reasonable dimensions (not collapsed)
        Assert.assertTrue("Item should have reasonable height",
            itemBounds.height() > 50) // Profile items should be at least 50dp tall

        Assert.assertTrue("Item should have reasonable width",
            itemBounds.width() > 200) // Profile items should be reasonably wide
    }
}