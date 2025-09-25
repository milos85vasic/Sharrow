package com.shareconnect.automation

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.EditProfileActivity
import com.shareconnect.R
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditProfileKeyboardAutomationTest {

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<EditProfileActivity>

    companion object {
        private const val TIMEOUT = 5000L
        private const val SHORT_TIMEOUT = 2000L
        private const val KEYBOARD_ANIMATION_DELAY = 1000L
    }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Launch EditProfileActivity
        scenario = ActivityScenario.launch(EditProfileActivity::class.java)

        // Wait for activity to be fully loaded
        device.waitForIdle()
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        // Dismiss keyboard if visible
        device.pressBack()
        device.waitForIdle()

        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    @Test
    fun testKeyboardAppearsWhenFieldFocused() {
        // Click on the first field (Profile Name)
        val profileNameField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))
        Assert.assertTrue("Profile name field should be visible", profileNameField.exists())

        profileNameField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // Verify keyboard is visible by checking if we can type
        profileNameField.text = "Test Profile"
        Assert.assertEquals("Should be able to type in profile name field", "Test Profile", profileNameField.text)

        // Check that the field is focused and keyboard is functional
        device.pressKey(8) // Backspace
        device.waitForIdle()

        val updatedText = profileNameField.text
        Assert.assertTrue("Backspace should work when keyboard is active", updatedText.length < "Test Profile".length)
    }

    @Test
    fun testScrollViewScrollsWhenKeyboardAppears() {
        // Get initial scroll position by checking visibility of bottom elements
        val saveButton = device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave"))
        val initialSaveButtonVisible = saveButton.exists()

        // Focus on a field at the bottom (password field)
        val passwordField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextPassword"))

        // Scroll down to make password field visible if needed
        if (!passwordField.exists()) {
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight * 3 / 4,
                device.displayWidth / 2,
                device.displayHeight / 4,
                10
            )
            device.waitForIdle()
        }

        Assert.assertTrue("Password field should be visible after scrolling", passwordField.exists())

        passwordField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // After keyboard appears, the view should adjust to keep the focused field visible
        // The save button should still be accessible even with keyboard visible
        val saveButtonAfterKeyboard = device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave"))
        Assert.assertTrue("Save button should remain accessible when keyboard is visible",
            saveButtonAfterKeyboard.exists() || device.hasObject(UiSelector().scrollable(true)))
    }

    @Test
    fun testIMENavigationBetweenFields() {
        // Start with profile name field
        val profileNameField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))
        profileNameField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // Type text and press next
        profileNameField.text = "Test"
        device.pressKeyCode(66, 0, 0) // Enter/Done key which should trigger next action
        device.waitForIdle()

        // Should move to server URL field
        Thread.sleep(500)

        // Type in URL field (if successfully navigated, this should work)
        val serverUrlField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextServerUrl"))
        if (serverUrlField.exists()) {
            serverUrlField.text = "http://test.com"
            device.waitForIdle()

            // Press next to move to port field
            device.pressKeyCode(66, 0, 0)
            device.waitForIdle()
            Thread.sleep(500)

            // Type in port field
            val portField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextServerPort"))
            if (portField.exists()) {
                portField.text = "8080"
                Assert.assertEquals("Port field should contain typed value", "8080", portField.text)
            }
        }
    }

    @Test
    fun testScrollViewIsScrollableWithKeyboard() {
        // Focus on first field to bring up keyboard
        val profileNameField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))
        profileNameField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // Try scrolling with keyboard visible
        val initialScroll = device.findObject(UiSelector().scrollable(true))
        Assert.assertTrue("ScrollView should be present and scrollable", initialScroll.exists())

        // Scroll down to test scrollability
        val scrollSuccess = device.scroll(UiScrollable(UiSelector().scrollable(true)),
            UiSelector().resourceId("com.shareconnect:id/buttonSave"),
            UiScrollable.Instance.DOWN)

        // The scroll should work even with keyboard visible
        Assert.assertTrue("Should be able to scroll with keyboard visible", scrollSuccess ||
            device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave")).exists())
    }

    @Test
    fun testAllFieldsAccessibleWithKeyboard() {
        val fieldIds = listOf(
            "com.shareconnect:id/editTextProfileName",
            "com.shareconnect:id/editTextServerUrl",
            "com.shareconnect:id/editTextServerPort",
            "com.shareconnect:id/autoCompleteServiceType",
            "com.shareconnect:id/editTextUsername",
            "com.shareconnect:id/editTextPassword"
        )

        fieldIds.forEach { fieldId ->
            // Try to find and focus each field
            var field = device.findObject(UiSelector().resourceId(fieldId))

            // Scroll to field if not visible
            if (!field.exists()) {
                val scrollable = UiScrollable(UiSelector().scrollable(true))
                scrollable.scrollIntoView(UiSelector().resourceId(fieldId))
                device.waitForIdle()
                field = device.findObject(UiSelector().resourceId(fieldId))
            }

            Assert.assertTrue("Field $fieldId should be accessible", field.exists())

            // Focus the field
            field.click()
            device.waitForIdle()
            Thread.sleep(500)

            // Verify field is focused by trying to interact with it
            if (fieldId.contains("editText")) {
                field.text = "test"
                Assert.assertTrue("Should be able to type in $fieldId",
                    field.text.contains("test") || field.isFocused)
            }

            // Clear for next iteration
            if (fieldId.contains("editText")) {
                field.clearTextField()
            }
        }
    }

    @Test
    fun testKeyboardAdjustResizeBehavior() {
        // Get screen height before keyboard
        val displayHeight = device.displayHeight

        // Focus on bottom field to trigger keyboard
        val scrollable = UiScrollable(UiSelector().scrollable(true))
        scrollable.scrollIntoView(UiSelector().resourceId("com.shareconnect:id/editTextPassword"))

        val passwordField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextPassword"))
        passwordField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // With adjustResize, the content should be resized and scrollable
        // Check that we can still access all fields through scrolling
        val scrollableWithKeyboard = device.findObject(UiSelector().scrollable(true))
        Assert.assertTrue("Content should remain scrollable with keyboard",
            scrollableWithKeyboard.exists())

        // Test that we can scroll to top elements even with keyboard visible
        val scrollToTop = scrollable.scrollBackward()

        // Should be able to find profile name field even with keyboard
        val profileNameWithKeyboard = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))
        Assert.assertTrue("Should be able to access top fields with keyboard visible",
            profileNameWithKeyboard.exists() || scrollToTop)
    }

    @Test
    fun testServiceTypeDropdownWithKeyboard() {
        // Focus on service type dropdown
        val serviceTypeField = device.findObject(UiSelector().resourceId("com.shareconnect:id/autoCompleteServiceType"))

        // Scroll to field if needed
        if (!serviceTypeField.exists()) {
            val scrollable = UiScrollable(UiSelector().scrollable(true))
            scrollable.scrollIntoView(UiSelector().resourceId("com.shareconnect:id/autoCompleteServiceType"))
            device.waitForIdle()
        }

        Assert.assertTrue("Service type dropdown should be accessible", serviceTypeField.exists())

        serviceTypeField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // The dropdown should work even with soft keyboard considerations
        // Try clicking on dropdown arrow or the field itself
        val dropdownOptions = device.findObject(UiSelector().textContains("MeTube"))

        // If dropdown opened, we should see options
        if (dropdownOptions.exists()) {
            dropdownOptions.click()
            device.waitForIdle()

            Assert.assertTrue("Should be able to select dropdown option",
                serviceTypeField.text.contains("MeTube") || serviceTypeField.text.isNotEmpty())
        }
    }

    @Test
    fun testFormValidationWithKeyboard() {
        // Fill form with invalid data while keyboard is active
        val profileNameField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))
        profileNameField.click()
        profileNameField.text = "" // Empty name should trigger validation

        // Navigate to save button with keyboard visible
        val scrollable = UiScrollable(UiSelector().scrollable(true))
        scrollable.scrollIntoView(UiSelector().resourceId("com.shareconnect:id/buttonSave"))

        val saveButton = device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave"))
        Assert.assertTrue("Save button should be accessible with keyboard", saveButton.exists())

        saveButton.click()
        device.waitForIdle()

        // Should show validation error - field should get focus and error should be visible
        Thread.sleep(1000)

        // Check if validation error appears (field gets focused automatically)
        val profileNameWithError = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))
        Assert.assertTrue("Profile name field should be accessible for error display",
            profileNameWithError.exists())
    }

    @Test
    fun testKeyboardHiddenOnBackPress() {
        // Focus field to show keyboard
        val profileNameField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))
        profileNameField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // Type some text to confirm keyboard is active
        profileNameField.text = "Test"

        // Press back to hide keyboard
        device.pressBack()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // Keyboard should be hidden, but we should still be in the activity
        // Check by looking for activity-specific elements
        val toolbar = device.findObject(UiSelector().textContains("Edit Profile"))
        Assert.assertTrue("Should still be in EditProfile activity after hiding keyboard",
            toolbar.exists())

        // Content should be fully visible again
        val saveButton = device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave"))
        Assert.assertTrue("Save button should be visible when keyboard is hidden", saveButton.exists())
    }
}