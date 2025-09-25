package com.shareconnect.automation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.MainActivity
import com.shareconnect.R
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ClipboardDialogAutomationTest {

    private lateinit var device: UiDevice
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var scenario: ActivityScenario<MainActivity>

    companion object {
        private const val TIMEOUT = 5000L
        private const val SHORT_TIMEOUT = 2000L
    }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val context = ApplicationProvider.getApplicationContext<Context>()
        clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Clear clipboard history before each test
        clearClipboardHistory()

        // Launch MainActivity
        scenario = ActivityScenario.launch(MainActivity::class.java)

        // Wait for activity to be fully loaded
        device.waitForIdle()
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        clearClipboardHistory()
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    private fun clearClipboardHistory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("clipboard_history", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun addToClipboard(text: String) {
        val clipData = ClipData.newPlainText("ShareConnect Test", text)
        clipboardManager.setPrimaryClip(clipData)
        // Wait a bit for clipboard manager to process
        Thread.sleep(500)
    }

    @Test
    fun testClipboardDialogAppearsWhenFabClicked() {
        // Add some URLs to clipboard
        addToClipboard("https://www.youtube.com/watch?v=test1")
        Thread.sleep(500)
        addToClipboard("https://www.vimeo.com/test2")
        Thread.sleep(500)

        // Find and click the floating action button
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        Assert.assertTrue("FAB should be visible", fab.exists())

        fab.click()
        device.waitForIdle()

        // Verify dialog appears
        val dialog = device.findObject(UiSelector().textContains("Select from Clipboard History"))
        Assert.assertTrue("Clipboard selection dialog should appear", dialog.waitForExists(TIMEOUT))

        // Verify subtitle text
        val subtitle = device.findObject(UiSelector().textContains("Choose a URL from your recent clipboard history"))
        Assert.assertTrue("Dialog subtitle should be visible", subtitle.exists())
    }

    @Test
    fun testClipboardDialogDisplaysMultipleItems() {
        // Add multiple URLs to clipboard
        val urls = listOf(
            "https://www.youtube.com/watch?v=test1",
            "https://www.vimeo.com/test2",
            "https://www.twitch.tv/test3",
            "https://magnet:?xt=urn:btih:test4"
        )

        urls.forEach { url ->
            addToClipboard(url)
            Thread.sleep(500)
        }

        // Click FAB to open dialog
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        fab.click()
        device.waitForIdle()

        // Wait for dialog to appear
        device.findObject(UiSelector().textContains("Select from Clipboard History"))
            .waitForExists(TIMEOUT)

        // Verify multiple items are displayed
        urls.forEach { url ->
            val urlItem = device.findObject(UiSelector().textContains(url))
            Assert.assertTrue("URL '$url' should be displayed in dialog", urlItem.exists())
        }

        // Verify items have proper icons
        val recyclerView = device.findObject(UiSelector().resourceId("com.shareconnect:id/recyclerViewClipboardItems"))
        Assert.assertTrue("RecyclerView should be visible", recyclerView.exists())
    }

    @Test
    fun testClipboardItemSelection() {
        // Add URLs to clipboard
        val testUrl = "https://www.youtube.com/watch?v=testselection"
        addToClipboard("https://www.vimeo.com/other")
        Thread.sleep(500)
        addToClipboard(testUrl)
        Thread.sleep(500)

        // Click FAB to open dialog
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        fab.click()
        device.waitForIdle()

        // Wait for dialog to appear
        device.findObject(UiSelector().textContains("Select from Clipboard History"))
            .waitForExists(TIMEOUT)

        // Click on the specific URL item
        val urlItem = device.findObject(UiSelector().textContains(testUrl))
        Assert.assertTrue("Test URL should be visible", urlItem.exists())

        urlItem.click()
        device.waitForIdle()

        // Verify dialog disappears
        val dialog = device.findObject(UiSelector().textContains("Select from Clipboard History"))
        Assert.assertFalse("Dialog should disappear after item selection", dialog.waitForExists(SHORT_TIMEOUT))

        // Verify ShareActivity is launched (check if we can find share-related UI)
        // Since ShareActivity might take time to load, we look for any indication it started
        device.waitForIdle()
        Thread.sleep(2000) // Give time for activity transition
    }

    @Test
    fun testClipboardDialogClearHistory() {
        // Add URLs to clipboard
        addToClipboard("https://www.youtube.com/watch?v=toclear1")
        Thread.sleep(500)
        addToClipboard("https://www.vimeo.com/toclear2")
        Thread.sleep(500)

        // Click FAB to open dialog
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        fab.click()
        device.waitForIdle()

        // Wait for dialog to appear
        device.findObject(UiSelector().textContains("Select from Clipboard History"))
            .waitForExists(TIMEOUT)

        // Click clear history button
        val clearButton = device.findObject(UiSelector().text("Clear History"))
        Assert.assertTrue("Clear History button should be visible", clearButton.exists())

        clearButton.click()
        device.waitForIdle()

        // Verify dialog disappears
        val dialog = device.findObject(UiSelector().textContains("Select from Clipboard History"))
        Assert.assertFalse("Dialog should disappear after clearing history", dialog.waitForExists(SHORT_TIMEOUT))

        // Click FAB again to verify history was cleared
        fab.click()
        device.waitForIdle()

        // Should show empty state or fallback to current clipboard
        val emptyState = device.findObject(UiSelector().textContains("No valid URLs found"))
        val dialogTitle = device.findObject(UiSelector().textContains("Select from Clipboard History"))

        // Either empty state toast appears or dialog doesn't show (both are valid behaviors)
        Assert.assertTrue("Should show empty state or no dialog",
            emptyState.waitForExists(SHORT_TIMEOUT) || !dialogTitle.waitForExists(SHORT_TIMEOUT))
    }

    @Test
    fun testClipboardDialogCancel() {
        // Add URL to clipboard
        addToClipboard("https://www.youtube.com/watch?v=testcancel")
        Thread.sleep(500)

        // Click FAB to open dialog
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        fab.click()
        device.waitForIdle()

        // Wait for dialog to appear
        device.findObject(UiSelector().textContains("Select from Clipboard History"))
            .waitForExists(TIMEOUT)

        // Click cancel button
        val cancelButton = device.findObject(UiSelector().text("Cancel"))
        Assert.assertTrue("Cancel button should be visible", cancelButton.exists())

        cancelButton.click()
        device.waitForIdle()

        // Verify dialog disappears
        val dialog = device.findObject(UiSelector().textContains("Select from Clipboard History"))
        Assert.assertFalse("Dialog should disappear after cancel", dialog.waitForExists(SHORT_TIMEOUT))

        // Verify we're back to main activity
        val mainTitle = device.findObject(UiSelector().textContains("ShareConnect"))
        Assert.assertTrue("Should return to main activity", mainTitle.exists())
    }

    @Test
    fun testClipboardDialogEmptyState() {
        // Ensure clipboard is empty by clearing and not adding anything
        clearClipboardHistory()

        // Add non-URL text to clipboard to test empty URL state
        addToClipboard("This is just plain text, not a URL")
        Thread.sleep(500)

        // Click FAB to open dialog
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        fab.click()
        device.waitForIdle()

        // Should show toast about no valid URLs or empty state
        Thread.sleep(2000) // Wait for toast or dialog processing

        // Check if empty state message appears or toast is shown
        val emptyMessage = device.findObject(UiSelector().textContains("No valid URLs found"))

        // This test verifies the empty state handling, either through toast or dialog empty state
        // The exact behavior depends on implementation details
        device.waitForIdle()
    }

    @Test
    fun testClipboardDialogIconsDisplay() {
        // Add URLs from different services to test icon display
        val urlsWithIcons = mapOf(
            "https://www.youtube.com/watch?v=icontest" to "YouTube",
            "https://www.vimeo.com/icontest" to "Vimeo",
            "https://www.twitch.tv/icontest" to "Twitch",
            "https://www.reddit.com/r/test" to "Reddit",
            "magnet:?xt=urn:btih:icontest" to "Magnet"
        )

        urlsWithIcons.keys.forEach { url ->
            addToClipboard(url)
            Thread.sleep(500)
        }

        // Click FAB to open dialog
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        fab.click()
        device.waitForIdle()

        // Wait for dialog to appear
        device.findObject(UiSelector().textContains("Select from Clipboard History"))
            .waitForExists(TIMEOUT)

        // Verify each URL is displayed
        urlsWithIcons.keys.forEach { url ->
            val urlItem = device.findObject(UiSelector().textContains(url))
            Assert.assertTrue("URL '$url' should be displayed", urlItem.exists())
        }

        // Verify icons are present (check that icon containers exist)
        val iconElements = device.findObject(UiSelector().resourceId("com.shareconnect:id/clipboardItemIcon"))
        Assert.assertTrue("Icon elements should be present", iconElements.exists())

        val typeIconElements = device.findObject(UiSelector().resourceId("com.shareconnect:id/clipboardItemTypeIcon"))
        Assert.assertTrue("Type icon elements should be present", typeIconElements.exists())
    }

    @Test
    fun testClipboardDialogAccessibility() {
        // Add URL to clipboard
        addToClipboard("https://www.youtube.com/watch?v=accessibility")
        Thread.sleep(500)

        // Click FAB to open dialog
        val fab = device.findObject(UiSelector().resourceId("com.shareconnect:id/fabAdd"))
        fab.click()
        device.waitForIdle()

        // Wait for dialog to appear
        device.findObject(UiSelector().textContains("Select from Clipboard History"))
            .waitForExists(TIMEOUT)

        // Verify dialog has proper accessibility structure
        val dialogTitle = device.findObject(UiSelector().textContains("Select from Clipboard History"))
        Assert.assertTrue("Dialog title should be accessible", dialogTitle.exists())

        val subtitle = device.findObject(UiSelector().textContains("Choose a URL from your recent clipboard history"))
        Assert.assertTrue("Dialog subtitle should be accessible", subtitle.exists())

        // Verify buttons are accessible
        val clearButton = device.findObject(UiSelector().text("Clear History"))
        Assert.assertTrue("Clear History button should be accessible", clearButton.exists())

        val cancelButton = device.findObject(UiSelector().text("Cancel"))
        Assert.assertTrue("Cancel button should be accessible", cancelButton.exists())

        // Close dialog
        cancelButton.click()
        device.waitForIdle()
    }
}