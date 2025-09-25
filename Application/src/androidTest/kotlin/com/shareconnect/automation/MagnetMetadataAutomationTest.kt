package com.shareconnect.automation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.shareconnect.ProfileManager
import com.shareconnect.R
import com.shareconnect.ServerProfile
import com.shareconnect.ShareActivity
import com.shareconnect.database.HistoryRepository
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MagnetMetadataAutomationTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private lateinit var profileManager: ProfileManager
    private lateinit var historyRepository: HistoryRepository
    private var testProfiles: MutableList<ServerProfile> = mutableListOf()

    companion object {
        // Test magnet links with different characteristics
        private const val MOVIE_MAGNET = "magnet:?xt=urn:btih:abcd1234567890abcdef&dn=The.Awesome.Movie.2023.1080p.BluRay.x264&xl=2147483648&tr=http://tracker.example.com/announce"
        private const val TV_SHOW_MAGNET = "magnet:?xt=urn:btih:1234567890abcdefghij&dn=Amazing.TV.Show.S01E01.720p.HDTV&xl=1073741824&tr=udp://tracker.example.com:8080"
        private const val MUSIC_MAGNET = "magnet:?xt=urn:btih:fedcba0987654321&dn=Great.Artist.Album.2023.FLAC&xl=536870912&tr=http://music.tracker.com/announce"
        private const val SOFTWARE_MAGNET = "magnet:?xt=urn:btih:0987654321fedcba&dn=Awesome.Game.v1.2.3.PC.Game&xl=10737418240&tr=http://game.tracker.com/announce"
        private const val BOOK_MAGNET = "magnet:?xt=urn:btih:abcdef1234567890&dn=Programming.Guide.2023.PDF&xl=104857600&tr=http://book.tracker.com/announce"
        private const val MINIMAL_MAGNET = "magnet:?xt=urn:btih:123456789abcdef0&dn=Simple+File"
        private const val NO_NAME_MAGNET = "magnet:?xt=urn:btih:abcdef0123456789"
    }

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        profileManager = ProfileManager(context)
        historyRepository = HistoryRepository(context)

        // Clear any existing profiles and history
        clearTestData()

        // Create a test qBittorrent profile
        val testProfile = createTestProfile("Test qBittorrent", ServerProfile.TYPE_TORRENT, ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        profileManager.setDefaultProfile(testProfile)

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
        // Clear test profiles
        testProfiles.forEach { profileManager.deleteProfile(it) }
        testProfiles.clear()

        // Clear test history
        historyRepository.deleteAllHistoryItems()
    }

    @Test
    fun testMovieMagnetMetadataExtraction() {
        // Test movie magnet link
        val scenario = launchShareActivity(MOVIE_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify movie title is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("The.Awesome.Movie.2023.1080p.BluRay.x264"))))

        // Verify size information is shown (should be around 2GB)
        verifyMetadataPresence("2.0 GB")

        // Verify content type inference
        verifyContentTypeInference("Movie")

        scenario.close()
    }

    @Test
    fun testTVShowMagnetMetadataExtraction() {
        // Test TV show magnet link
        val scenario = launchShareActivity(TV_SHOW_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify TV show title is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("Amazing.TV.Show.S01E01.720p.HDTV"))))

        // Verify size information is shown (should be around 1GB)
        verifyMetadataPresence("1.0 GB")

        // Verify content type inference
        verifyContentTypeInference("TV Show")

        scenario.close()
    }

    @Test
    fun testMusicMagnetMetadataExtraction() {
        // Test music magnet link
        val scenario = launchShareActivity(MUSIC_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify music title is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("Great.Artist.Album.2023.FLAC"))))

        // Verify size information is shown (should be around 512MB)
        verifyMetadataPresence("512.0 MB")

        // Verify content type inference
        verifyContentTypeInference("Music")

        scenario.close()
    }

    @Test
    fun testSoftwareMagnetMetadataExtraction() {
        // Test software/game magnet link
        val scenario = launchShareActivity(SOFTWARE_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify software title is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("Awesome.Game.v1.2.3.PC.Game"))))

        // Verify size information is shown (should be around 10GB)
        verifyMetadataPresence("10.0 GB")

        // Verify content type inference
        verifyContentTypeInference("Software/Game")

        scenario.close()
    }

    @Test
    fun testBookMagnetMetadataExtraction() {
        // Test book/document magnet link
        val scenario = launchShareActivity(BOOK_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify book title is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("Programming.Guide.2023.PDF"))))

        // Verify size information is shown (should be around 100MB)
        verifyMetadataPresence("100.0 MB")

        // Verify content type inference
        verifyContentTypeInference("Book/Document")

        scenario.close()
    }

    @Test
    fun testMinimalMagnetMetadataExtraction() {
        // Test magnet with minimal information
        val scenario = launchShareActivity(MINIMAL_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify basic title is displayed (URL decoded)
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("Simple File"))))

        // Verify basic BitTorrent description is present
        verifyMetadataPresence("BitTorrent magnet link")

        // Verify info hash is shown
        verifyMetadataPresence("12345678...")

        scenario.close()
    }

    @Test
    fun testNoNameMagnetMetadataExtraction() {
        // Test magnet without display name
        val scenario = launchShareActivity(NO_NAME_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify fallback title is used
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("Magnet Link"))))

        // Verify basic BitTorrent description is present
        verifyMetadataPresence("BitTorrent magnet link")

        // Verify info hash is shown
        verifyMetadataPresence("abcdef01...")

        scenario.close()
    }

    @Test
    fun testMagnetMetadataInHistory() {
        // Test that magnet metadata is properly saved to history
        val scenario = launchShareActivity(MOVIE_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Send to qBittorrent (this will save to history)
        onView(withId(R.id.buttonSendToMeTube)).perform(androidx.test.espresso.action.ViewActions.click())

        // Wait for sending to complete
        Thread.sleep(2000)

        scenario.close()

        // Verify history contains the magnet metadata
        val historyItems = historyRepository.allHistoryItems
        val magnetHistory = historyItems.find { it.url == MOVIE_MAGNET }

        assert(magnetHistory != null) { "Magnet link should be saved to history" }
        assert(magnetHistory!!.title == "The.Awesome.Movie.2023.1080p.BluRay.x264") { "History should contain extracted title" }
        assert(magnetHistory.description?.contains("2.0 GB") == true) { "History should contain size information" }
        assert(magnetHistory.description?.contains("BitTorrent magnet link") == true) { "History should contain magnet description" }
        assert(magnetHistory.serviceProvider == "Movie") { "History should contain inferred content type" }
    }

    @Test
    fun testMagnetMetadataHashExtraction() {
        // Test that info hash is properly extracted and displayed
        val scenario = launchShareActivity(MOVIE_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify hash information is displayed (truncated)
        verifyMetadataPresence("abcd1234...")

        scenario.close()
    }

    @Test
    fun testMagnetTrackerInformation() {
        // Test that tracker information is included in metadata
        val scenario = launchShareActivity(MOVIE_MAGNET)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify tracker count information is displayed
        verifyMetadataPresence("1 tracker(s)")

        scenario.close()
    }

    @Test
    fun testMagnetSizeFormatting() {
        // Test different size formats
        val testCases = listOf(
            Pair(SOFTWARE_MAGNET, "10.0 GB"), // Large file
            Pair(MUSIC_MAGNET, "512.0 MB"),   // Medium file
            Pair(BOOK_MAGNET, "100.0 MB")     // Small file
        )

        for ((magnetLink, expectedSize) in testCases) {
            val scenario = launchShareActivity(magnetLink)

            // Wait for metadata to be fetched
            device.waitForIdle()
            Thread.sleep(3000)

            // Verify correct size formatting
            verifyMetadataPresence(expectedSize)

            scenario.close()
        }
    }

    @Test
    fun testMagnetURLDecoding() {
        // Test that URL encoding in magnet names is properly decoded
        val encodedMagnet = "magnet:?xt=urn:btih:abcd1234&dn=Movie+Title+With+Spaces%20And%20Special%21%40%23"
        val scenario = launchShareActivity(encodedMagnet)

        // Wait for metadata to be fetched
        device.waitForIdle()
        Thread.sleep(3000)

        // Verify decoded title is displayed
        onView(withId(R.id.textViewYouTubeLink))
            .check(matches(withText(containsString("Movie Title With Spaces And Special!@#"))))

        scenario.close()
    }

    private fun launchShareActivity(magnetLink: String): ActivityScenario<ShareActivity> {
        val intent = Intent(context, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, magnetLink)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return ActivityScenario.launch(intent)
    }

    private fun verifyMetadataPresence(expectedText: String) {
        // Check if the expected text appears anywhere in the activity
        val hasText = device.hasObject(By.textContains(expectedText))
        assert(hasText) { "Expected text '$expectedText' should be present in the UI" }
    }

    private fun verifyContentTypeInference(expectedType: String) {
        // Content type is shown in the service provider field or metadata description
        val hasContentType = device.hasObject(By.textContains(expectedType))
        assert(hasContentType) { "Expected content type '$expectedType' should be inferred and displayed" }
    }

    private fun createTestProfile(
        name: String,
        serviceType: String,
        torrentClientType: String? = null
    ): ServerProfile {
        val profile = ServerProfile()
        profile.id = "test-$name-${System.currentTimeMillis()}-${Math.random()}"
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