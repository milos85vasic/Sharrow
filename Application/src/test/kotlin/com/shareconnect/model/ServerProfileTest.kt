package com.shareconnect.model

import com.shareconnect.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
        assertNull(serverProfile.username)
        assertNull(serverProfile.password)
    }

    @Test
    fun testServerProfileSettersAndGetters() {
        val id = "test-id-123"
        val name = "Test Profile"
        val url = "http://example.com"
        val port = 8080
        val serviceType = ServerProfile.TYPE_METUBE
        val torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT
        val username = "testuser"
        val password = "testpass"

        serverProfile.id = id
        serverProfile.name = name
        serverProfile.url = url
        serverProfile.port = port
        serverProfile.serviceType = serviceType
        serverProfile.torrentClientType = torrentClientType
        serverProfile.username = username
        serverProfile.password = password

        assertEquals(id, serverProfile.id)
        assertEquals(name, serverProfile.name)
        assertEquals(url, serverProfile.url)
        assertEquals(port, serverProfile.port)
        assertEquals(serviceType, serverProfile.serviceType)
        assertEquals(torrentClientType, serverProfile.torrentClientType)
        assertEquals(username, serverProfile.username)
        assertEquals(password, serverProfile.password)
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
        profile1.url = "http://example.com"
        profile1.port = 8080
        profile1.serviceType = ServerProfile.TYPE_METUBE
        profile1.torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT
        profile1.username = "testuser"
        profile1.password = "testpass"

        profile2.id = "test-id"
        profile2.name = "Test Profile"
        profile2.url = "http://example.com"
        profile2.port = 8080
        profile2.serviceType = ServerProfile.TYPE_METUBE
        profile2.torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT
        profile2.username = "testuser"
        profile2.password = "testpass"

        assertEquals(profile1, profile2)
        
        // Test inequality with different username
        val profile3 = ServerProfile()
        profile3.id = profile1.id
        profile3.name = profile1.name
        profile3.url = profile1.url
        profile3.port = profile1.port
        profile3.serviceType = profile1.serviceType
        profile3.torrentClientType = profile1.torrentClientType
        profile3.username = "differentuser"
        profile3.password = profile1.password
        assertNotEquals(profile1, profile3)
        
        // Test inequality with different password
        val profile4 = ServerProfile()
        profile4.id = profile1.id
        profile4.name = profile1.name
        profile4.url = profile1.url
        profile4.port = profile1.port
        profile4.serviceType = profile1.serviceType
        profile4.torrentClientType = profile1.torrentClientType
        profile4.username = profile1.username
        profile4.password = "differentpass"
        assertNotEquals(profile1, profile4)
    }
}