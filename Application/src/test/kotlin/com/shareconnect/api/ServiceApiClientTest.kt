package com.shareconnect.api

import com.shareconnect.ServiceApiClient
import com.shareconnect.ServerProfile
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ServiceApiClientTest {

    private lateinit var serviceApiClient: ServiceApiClient
    private lateinit var testProfile: ServerProfile

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        serviceApiClient = ServiceApiClient()

        testProfile = ServerProfile().apply {
            id = "test-profile"
            name = "Test Profile"
            url = "http://test.example.com"
            port = 8080
            serviceType = ServerProfile.TYPE_METUBE
        }
    }

    @Test
    fun testServiceApiClientInitialization() {
        assertNotNull(serviceApiClient)
    }

    @Test
    fun testServiceApiClientCreation() {
        assertNotNull(serviceApiClient)
    }

    @Test
    fun testSendUrlToServiceCallback() {
        val url = "https://www.youtube.com/watch?v=test"

        val callback = object : ServiceApiClient.ServiceApiCallback {
            override fun onSuccess() {}
            override fun onError(error: String?) {}
        }

        // Test that method exists and can be called
        serviceApiClient.sendUrlToService(testProfile, url, callback)
    }

    @Test
    fun testMeTubeProfile() {
        testProfile.serviceType = ServerProfile.TYPE_METUBE
        assertEquals(ServerProfile.TYPE_METUBE, testProfile.serviceType)
    }

    @Test
    fun testYtdlProfile() {
        testProfile.serviceType = ServerProfile.TYPE_YTDL
        assertEquals(ServerProfile.TYPE_YTDL, testProfile.serviceType)
    }

    @Test
    fun testTorrentProfile() {
        testProfile.serviceType = ServerProfile.TYPE_TORRENT
        assertEquals(ServerProfile.TYPE_TORRENT, testProfile.serviceType)
    }

    @Test
    fun testJDownloaderProfile() {
        testProfile.serviceType = ServerProfile.TYPE_JDOWNLOADER
        assertEquals(ServerProfile.TYPE_JDOWNLOADER, testProfile.serviceType)
    }

    @Test
    fun testServiceApiCallback() {
        val callback = object : ServiceApiClient.ServiceApiCallback {
            override fun onSuccess() {}
            override fun onError(error: String?) {}
        }
        assertNotNull(callback)
    }

    @Test
    fun testProfileTypes() {
        assertEquals("metube", ServerProfile.TYPE_METUBE)
        assertEquals("ytdl", ServerProfile.TYPE_YTDL)
        assertEquals("torrent", ServerProfile.TYPE_TORRENT)
        assertEquals("jdownloader", ServerProfile.TYPE_JDOWNLOADER)
    }

    @Test
    fun testUnsupportedServiceType() {
        val unsupportedProfile = ServerProfile().apply {
            id = "unsupported"
            name = "Unsupported"
            url = "http://localhost"
            port = 8080
            serviceType = "unsupported"
        }

        val callback = object : ServiceApiClient.ServiceApiCallback {
            var errorReceived = false
            override fun onSuccess() {}
            override fun onError(error: String?) {
                errorReceived = true
            }
        }

        serviceApiClient.sendUrlToService(unsupportedProfile, "https://test.com", callback)
    }
}