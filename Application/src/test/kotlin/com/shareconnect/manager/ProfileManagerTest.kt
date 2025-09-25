package com.shareconnect.manager

import com.shareconnect.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Simple unit tests for ServerProfile data class functionality.
 * ProfileManager integration tests are in ProfileManagerInstrumentationTest.
 */
class ProfileManagerTest {

    @Test
    fun testServerProfileCreation() {
        val profile = ServerProfile()
        assertNotNull(profile)
    }

    @Test
    fun testServerProfileWithValues() {
        val profile = ServerProfile()
        profile.id = "test-id"
        profile.name = "Test Profile"
        profile.url = "http://example.com"
        profile.port = 8080
        profile.serviceType = ServerProfile.TYPE_METUBE
        profile.username = "testuser"
        profile.password = "testpass"

        assertEquals("test-id", profile.id)
        assertEquals("Test Profile", profile.name)
        assertEquals("http://example.com", profile.url)
        assertEquals(8080, profile.port)
        assertEquals(ServerProfile.TYPE_METUBE, profile.serviceType)
        assertEquals("testuser", profile.username)
        assertEquals("testpass", profile.password)
    }

    @Test
    fun testServiceTypeChecks() {
        val metubeProfile = ServerProfile()
        metubeProfile.serviceType = ServerProfile.TYPE_METUBE
        assertTrue(metubeProfile.isMeTube())
        assertFalse(metubeProfile.isYtDl())
        assertFalse(metubeProfile.isTorrent())
        assertFalse(metubeProfile.isJDownloader())

        val torrentProfile = ServerProfile()
        torrentProfile.serviceType = ServerProfile.TYPE_TORRENT
        assertFalse(torrentProfile.isMeTube())
        assertFalse(torrentProfile.isYtDl())
        assertTrue(torrentProfile.isTorrent())
        assertFalse(torrentProfile.isJDownloader())

        val ytdlProfile = ServerProfile()
        ytdlProfile.serviceType = ServerProfile.TYPE_YTDL
        assertFalse(ytdlProfile.isMeTube())
        assertTrue(ytdlProfile.isYtDl())
        assertFalse(ytdlProfile.isTorrent())
        assertFalse(ytdlProfile.isJDownloader())

        val jdownloaderProfile = ServerProfile()
        jdownloaderProfile.serviceType = ServerProfile.TYPE_JDOWNLOADER
        assertFalse(jdownloaderProfile.isMeTube())
        assertFalse(jdownloaderProfile.isYtDl())
        assertFalse(jdownloaderProfile.isTorrent())
        assertTrue(jdownloaderProfile.isJDownloader())
    }

    @Test
    fun testTorrentClientTypes() {
        val profile = ServerProfile()
        profile.serviceType = ServerProfile.TYPE_TORRENT
        profile.torrentClientType = ServerProfile.TORRENT_CLIENT_QBITTORRENT

        assertEquals(ServerProfile.TORRENT_CLIENT_QBITTORRENT, profile.torrentClientType)
        assertEquals("qBittorrent", profile.getTorrentClientName())
    }

    @Test
    fun testEqualsAndHashCode() {
        val profile1 = ServerProfile()
        profile1.id = "test-id"
        profile1.name = "Test Profile"
        profile1.url = "http://example.com"
        profile1.port = 8080

        val profile2 = ServerProfile()
        profile2.id = "test-id"
        profile2.name = "Test Profile"
        profile2.url = "http://example.com"
        profile2.port = 8080

        assertEquals(profile1, profile2)
        assertEquals(profile1.hashCode(), profile2.hashCode())
    }
}