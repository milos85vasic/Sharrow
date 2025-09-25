package com.shareconnect.utils

import com.shareconnect.ServerProfile
import org.junit.Assert.*
import org.junit.Test

class UrlCompatibilityUtilsTest {

    @Test
    fun testDetectUrlType_StreamingUrls() {
        // YouTube URLs
        assertEquals(UrlCompatibilityUtils.UrlType.STREAMING,
            UrlCompatibilityUtils.detectUrlType("https://www.youtube.com/watch?v=abc123"))
        assertEquals(UrlCompatibilityUtils.UrlType.STREAMING,
            UrlCompatibilityUtils.detectUrlType("https://youtu.be/abc123"))

        // Vimeo URLs
        assertEquals(UrlCompatibilityUtils.UrlType.STREAMING,
            UrlCompatibilityUtils.detectUrlType("https://vimeo.com/123456"))

        // Twitch URLs
        assertEquals(UrlCompatibilityUtils.UrlType.STREAMING,
            UrlCompatibilityUtils.detectUrlType("https://www.twitch.tv/streamer"))

        // Other streaming platforms
        assertEquals(UrlCompatibilityUtils.UrlType.STREAMING,
            UrlCompatibilityUtils.detectUrlType("https://soundcloud.com/artist/song"))
        assertEquals(UrlCompatibilityUtils.UrlType.STREAMING,
            UrlCompatibilityUtils.detectUrlType("https://www.dailymotion.com/video/xyz"))
    }

    @Test
    fun testDetectUrlType_TorrentUrls() {
        // Magnet links
        assertEquals(UrlCompatibilityUtils.UrlType.TORRENT,
            UrlCompatibilityUtils.detectUrlType("magnet:?xt=urn:btih:abc123"))

        // Torrent files
        assertEquals(UrlCompatibilityUtils.UrlType.TORRENT,
            UrlCompatibilityUtils.detectUrlType("https://example.com/file.torrent"))
        assertEquals(UrlCompatibilityUtils.UrlType.TORRENT,
            UrlCompatibilityUtils.detectUrlType("http://tracker.com/download.torrent"))
    }

    @Test
    fun testDetectUrlType_DirectDownloadUrls() {
        // Direct download URLs
        assertEquals(UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD,
            UrlCompatibilityUtils.detectUrlType("https://example.com/file.zip"))
        assertEquals(UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD,
            UrlCompatibilityUtils.detectUrlType("http://download.com/software.exe"))
        assertEquals(UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD,
            UrlCompatibilityUtils.detectUrlType("https://cdn.example.com/image.jpg"))
    }

    @Test
    fun testDetectUrlType_InvalidUrls() {
        assertNull(UrlCompatibilityUtils.detectUrlType(null))
        assertNull(UrlCompatibilityUtils.detectUrlType(""))
        assertNull(UrlCompatibilityUtils.detectUrlType("   "))
        assertNull(UrlCompatibilityUtils.detectUrlType("invalid-url"))
        assertNull(UrlCompatibilityUtils.detectUrlType("ftp://example.com/file.txt"))
    }

    @Test
    fun testIsProfileCompatible_MeTube() {
        val metubeProfile = ServerProfile().apply {
            serviceType = ServerProfile.TYPE_METUBE
        }

        // MeTube supports streaming only
        assertTrue(UrlCompatibilityUtils.isProfileCompatible(metubeProfile, UrlCompatibilityUtils.UrlType.STREAMING))
        assertFalse(UrlCompatibilityUtils.isProfileCompatible(metubeProfile, UrlCompatibilityUtils.UrlType.TORRENT))
        assertFalse(UrlCompatibilityUtils.isProfileCompatible(metubeProfile, UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD))
    }

    @Test
    fun testIsProfileCompatible_YTDL() {
        val ytdlProfile = ServerProfile().apply {
            serviceType = ServerProfile.TYPE_YTDL
        }

        // YT-DLP supports streaming and direct downloads
        assertTrue(UrlCompatibilityUtils.isProfileCompatible(ytdlProfile, UrlCompatibilityUtils.UrlType.STREAMING))
        assertFalse(UrlCompatibilityUtils.isProfileCompatible(ytdlProfile, UrlCompatibilityUtils.UrlType.TORRENT))
        assertTrue(UrlCompatibilityUtils.isProfileCompatible(ytdlProfile, UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD))
    }

    @Test
    fun testIsProfileCompatible_Torrent() {
        val torrentProfile = ServerProfile().apply {
            serviceType = ServerProfile.TYPE_TORRENT
        }

        // Torrent clients support torrents only
        assertFalse(UrlCompatibilityUtils.isProfileCompatible(torrentProfile, UrlCompatibilityUtils.UrlType.STREAMING))
        assertTrue(UrlCompatibilityUtils.isProfileCompatible(torrentProfile, UrlCompatibilityUtils.UrlType.TORRENT))
        assertFalse(UrlCompatibilityUtils.isProfileCompatible(torrentProfile, UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD))
    }

    @Test
    fun testIsProfileCompatible_JDownloader() {
        val jdownloaderProfile = ServerProfile().apply {
            serviceType = ServerProfile.TYPE_JDOWNLOADER
        }

        // jDownloader supports direct downloads and streaming
        assertTrue(UrlCompatibilityUtils.isProfileCompatible(jdownloaderProfile, UrlCompatibilityUtils.UrlType.STREAMING))
        assertFalse(UrlCompatibilityUtils.isProfileCompatible(jdownloaderProfile, UrlCompatibilityUtils.UrlType.TORRENT))
        assertTrue(UrlCompatibilityUtils.isProfileCompatible(jdownloaderProfile, UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD))
    }

    @Test
    fun testFilterCompatibleProfiles() {
        val metubeProfile = ServerProfile().apply {
            name = "MeTube"
            serviceType = ServerProfile.TYPE_METUBE
        }
        val torrentProfile = ServerProfile().apply {
            name = "qBittorrent"
            serviceType = ServerProfile.TYPE_TORRENT
        }
        val ytdlProfile = ServerProfile().apply {
            name = "YT-DLP"
            serviceType = ServerProfile.TYPE_YTDL
        }

        val allProfiles = listOf(metubeProfile, torrentProfile, ytdlProfile)

        // YouTube URL should show streaming profiles
        val youtubeCompatible = UrlCompatibilityUtils.filterCompatibleProfiles(
            allProfiles, "https://www.youtube.com/watch?v=test")
        assertEquals(2, youtubeCompatible.size)
        assertTrue(youtubeCompatible.contains(metubeProfile))
        assertTrue(youtubeCompatible.contains(ytdlProfile))
        assertFalse(youtubeCompatible.contains(torrentProfile))

        // Magnet URL should show torrent profiles only
        val magnetCompatible = UrlCompatibilityUtils.filterCompatibleProfiles(
            allProfiles, "magnet:?xt=urn:btih:test")
        assertEquals(1, magnetCompatible.size)
        assertTrue(magnetCompatible.contains(torrentProfile))
        assertFalse(magnetCompatible.contains(metubeProfile))
        assertFalse(magnetCompatible.contains(ytdlProfile))

        // Direct download URL should show download-capable profiles
        val downloadCompatible = UrlCompatibilityUtils.filterCompatibleProfiles(
            allProfiles, "https://example.com/file.zip")
        assertEquals(1, downloadCompatible.size)
        assertTrue(downloadCompatible.contains(ytdlProfile))
        assertFalse(downloadCompatible.contains(metubeProfile))
        assertFalse(downloadCompatible.contains(torrentProfile))
    }

    @Test
    fun testGetProfileSupportDescription() {
        val metubeProfile = ServerProfile().apply { serviceType = ServerProfile.TYPE_METUBE }
        val torrentProfile = ServerProfile().apply { serviceType = ServerProfile.TYPE_TORRENT }
        val ytdlProfile = ServerProfile().apply { serviceType = ServerProfile.TYPE_YTDL }
        val jdownloaderProfile = ServerProfile().apply { serviceType = ServerProfile.TYPE_JDOWNLOADER }

        assertEquals("Streaming videos (YouTube, Vimeo, etc.)",
            UrlCompatibilityUtils.getProfileSupportDescription(metubeProfile))
        assertEquals("Torrent files and magnet links",
            UrlCompatibilityUtils.getProfileSupportDescription(torrentProfile))
        assertEquals("Streaming videos and direct downloads",
            UrlCompatibilityUtils.getProfileSupportDescription(ytdlProfile))
        assertEquals("Direct downloads and streaming videos",
            UrlCompatibilityUtils.getProfileSupportDescription(jdownloaderProfile))
    }

    @Test
    fun testGetUrlTypeDescription() {
        assertEquals("streaming video",
            UrlCompatibilityUtils.getUrlTypeDescription(UrlCompatibilityUtils.UrlType.STREAMING))
        assertEquals("torrent",
            UrlCompatibilityUtils.getUrlTypeDescription(UrlCompatibilityUtils.UrlType.TORRENT))
        assertEquals("direct download",
            UrlCompatibilityUtils.getUrlTypeDescription(UrlCompatibilityUtils.UrlType.DIRECT_DOWNLOAD))
    }
}