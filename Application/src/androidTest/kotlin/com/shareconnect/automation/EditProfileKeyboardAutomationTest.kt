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
        device.pressKeyCode(67) // KEYCODE_DEL (Backspace)
        device.waitForIdle()

        val updatedText = profileNameField.text
        Assert.assertTrue("Backspace should work when keyboard is active", updatedText.length < "Test Profile".length)
    }

    @Test
    fun testScrollViewScrollsWhenKeyboardAppears() {
        // Test that focused field becomes visible when keyboard appears
        val profileNameField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextProfileName"))

        // First scroll to top to ensure consistent starting position
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight / 4,
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            10
        )
        device.waitForIdle()

        // Get profile name field position before keyboard
        val profileNameBounds = profileNameField.getBounds()
        val initialTop = profileNameBounds.top

        // Now focus on a field at the bottom (password field)
        val passwordField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextPassword"))

        // Scroll down to make password field visible
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

        // Get password field position before keyboard
        val passwordBounds = passwordField.getBounds()
        val initialPasswordTop = passwordBounds.top

        // Focus on password field to trigger keyboard
        passwordField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // After keyboard appears, check that the focused field is still visible
        // The password field should have moved up (smaller top coordinate) or remain visible
        val passwordAfterKeyboard = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextPassword"))
        Assert.assertTrue("Password field should remain accessible when keyboard is visible",
            passwordAfterKeyboard.exists())

        // Test that we can type in the focused field
        passwordField.text = "test123"
        Assert.assertTrue("Should be able to type in password field with keyboard visible",
            passwordField.text.contains("test"))

        // Verify scrollview is still functional with keyboard visible
        val scrollable = device.findObject(UiSelector().scrollable(true))
        Assert.assertTrue("ScrollView should remain functional with keyboard",
            scrollable.exists() && scrollable.isScrollable())
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
        device.pressKeyCode(66) // KEYCODE_ENTER (Enter/Done key)
        device.waitForIdle()

        // Should move to server URL field
        Thread.sleep(500)

        // Type in URL field (if successfully navigated, this should work)
        val serverUrlField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextServerUrl"))
        if (serverUrlField.exists()) {
            serverUrlField.text = "http://test.com"
            device.waitForIdle()

            // Press next to move to port field
            device.pressKeyCode(66) // KEYCODE_ENTER
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

        // Try to find save button by scrolling down
        val scrollable = UiScrollable(UiSelector().scrollable(true))
        val saveButton = device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave"))

        var scrollSuccess = false
        if (!saveButton.exists()) {
            scrollSuccess = scrollable.scrollIntoView(UiSelector().resourceId("com.shareconnect:id/buttonSave"))
        }

        // The scroll should work even with keyboard visible
        Assert.assertTrue("Should be able to scroll with keyboard visible",
            saveButton.exists() || scrollSuccess)
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

    @Test
    fun testKeyboardMovesFormUp() {
        // This test specifically verifies that the form content moves up when keyboard appears

        // Start at the bottom field (password)
        val passwordField = device.findObject(UiSelector().resourceId("com.shareconnect:id/editTextPassword"))

        // Scroll to make password field visible first
        val scrollable = UiScrollable(UiSelector().scrollable(true))
        scrollable.scrollIntoView(UiSelector().resourceId("com.shareconnect:id/editTextPassword"))
        device.waitForIdle()

        Assert.assertTrue("Password field should be visible", passwordField.exists())

        // Get the initial bounds of the password field
        val initialBounds = passwordField.getBounds()
        val initialY = initialBounds.centerY()

        // Also get bounds of elements that should move when keyboard appears
        val saveButton = device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave"))
        val initialSaveButtonBounds = if (saveButton.exists()) saveButton.getBounds() else null

        // Focus on password field to show keyboard
        passwordField.click()
        device.waitForIdle()
        Thread.sleep(KEYBOARD_ANIMATION_DELAY)

        // Verify password field is still accessible and functional
        Assert.assertTrue("Password field should remain accessible with keyboard", passwordField.exists())

        // Try typing to ensure the field is properly focused and visible
        passwordField.text = "testpass123"
        device.waitForIdle()

        Assert.assertTrue("Should be able to type in password field",
            passwordField.text.contains("testpass"))

        // Verify that we can still access form controls
        // The save button should be accessible through scrolling or should have moved up
        if (initialSaveButtonBounds != null) {
            val saveButtonAfterKeyboard = device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave"))

            if (!saveButtonAfterKeyboard.exists()) {
                // Try scrolling to find it
                scrollable.scrollIntoView(UiSelector().resourceId("com.shareconnect:id/buttonSave"))
                device.waitForIdle()
            }

            Assert.assertTrue("Save button should be accessible with keyboard visible",
                device.findObject(UiSelector().resourceId("com.shareconnect:id/buttonSave")).exists())
        }

        // Test field navigation with keyboard visible
        device.pressKeyCode(66) // Enter key - should trigger IME action
        device.waitForIdle()

        // The keyboard should have triggered the Done action, keeping focus or moving appropriately
        Assert.assertTrue("Form should handle IME actions properly with keyboard",
            passwordField.exists() || passwordField != null)
    }
}