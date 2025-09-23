package com.shareconnect.model

import com.shareconnect.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ServerProfileTest {

    private lateinit var serverProfile: ServerProfile

    @Before
    fun setUp() {
        serverProfile = ServerProfile()
    }

    @Test
    fun testServerProfileInitialization() {
        assertNotNull(serverProfile)
        assertNull(serverProfile.id)
        assertNull(serverProfile.name)
        assertNull(serverProfile.url)
        assertEquals(0, serverProfile.port)
        assertEquals(ServerProfile.TYPE_METUBE, serverProfile.serviceType)
        assertNull(serverProfile.torrentClientType)
    }

    @Test
    fun testServerProfileSettersAndGetters() {
        val id = "test-id-123"
        val name = "Test Profile"
        val url = "http://example.com"
        val port = 8080
        val serviceType = ServerProfile.TYPE_METUBE
        val torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT

        serverProfile.id = id
        serverProfile.name = name
        serverProfile.url = url
        serverProfile.port = port
        serverProfile.serviceType = serviceType
        serverProfile.torrentClientType = torrentClientType

        assertEquals(id, serverProfile.id)
        assertEquals(name, serverProfile.name)
        assertEquals(url, serverProfile.url)
        assertEquals(port, serverProfile.port)
        assertEquals(serviceType, serverProfile.serviceType)
        assertEquals(torrentClientType, serverProfile.torrentClientType)
    }

    @Test
    fun testServerProfileConstants() {
        assertEquals("metube", ServerProfile.TYPE_METUBE)
        assertEquals("ytdl", ServerProfile.TYPE_YTDL)
        assertEquals("torrent", ServerProfile.TYPE_TORRENT)
        assertEquals("jdownloader", ServerProfile.TYPE_JDOWNLOADER)

        assertEquals("qbittorrent", ServerProfile.TORRENT_CLIENT_QBITTORRENT)
        assertEquals("transmission", ServerProfile.TORRENT_CLIENT_TRANSMISSION)
        assertEquals("utorrent", ServerProfile.TORRENT_CLIENTUTORRENT)
    }

    @Test
    fun testProfileTypeChecking() {
        serverProfile.serviceType = ServerProfile.TYPE_METUBE
        assertTrue(serverProfile.isMeTube())
        assertFalse(serverProfile.isYtDl())
        assertFalse(serverProfile.isTorrent())
        assertFalse(serverProfile.isJDownloader())
    }

    @Test
    fun testServiceTypeName() {
        serverProfile.serviceType = ServerProfile.TYPE_YTDL
        assertTrue(serverProfile.isYtDl())

        val serviceName = serverProfile.getServiceTypeName()
        assertNotNull(serviceName)
    }

    @Test
    fun testTorrentClientName() {
        serverProfile.serviceType = ServerProfile.TYPE_TORRENT
        serverProfile.torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT

        assertTrue(serverProfile.isTorrent())
        val clientName = serverProfile.getTorrentClientName()
        assertNotNull(clientName)
    }

    @Test
    fun testEquality() {
        val profile1 = ServerProfile()
        val profile2 = ServerProfile()

        profile1.id = "test-id"
        profile1.name = "Test Profile"

        profile2.id = "test-id"
        profile2.name = "Test Profile"

        assertEquals(profile1.id, profile2.id)
        assertEquals(profile1.name, profile2.name)
    }
}