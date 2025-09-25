package com.shareconnect

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MetadataFetcherTest {

    private lateinit var metadataFetcher: MetadataFetcher

    @Before
    fun setUp() {
        metadataFetcher = MetadataFetcher()
    }

    @Test
    fun testMagnetMetadataExtraction_MovieWithFullInfo() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234567890abcdef&dn=The.Awesome.Movie.2023.1080p.BluRay.x264&xl=2147483648&tr=http://tracker.example.com/announce"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("The.Awesome.Movie.2023.1080p.BluRay.x264", metadata.title)
        assertTrue(metadata.description!!.contains("BitTorrent magnet link"))
        assertTrue(metadata.description!!.contains("2.0 GB"))
        assertTrue(metadata.description!!.contains("abcd1234..."))
        assertTrue(metadata.description!!.contains("1 tracker(s)"))
        assertEquals("Movie", metadata.siteName)
    }

    @Test
    fun testMagnetMetadataExtraction_TVShowWithMultipleTrackers() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:1234567890abcdefghij&dn=Amazing.TV.Show.S01E01.720p.HDTV&xl=1073741824&tr=http://tracker1.example.com/announce&tr=udp://tracker2.example.com:8080"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Amazing.TV.Show.S01E01.720p.HDTV", metadata.title)
        assertTrue(metadata.description!!.contains("1.0 GB"))
        assertTrue(metadata.description!!.contains("12345678..."))
        assertTrue(metadata.description!!.contains("2 tracker(s)"))
        assertEquals("TV Show", metadata.siteName)
    }

    @Test
    fun testMagnetMetadataExtraction_MusicAlbum() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:fedcba0987654321&dn=Great.Artist.Album.2023.FLAC&xl=536870912"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Great.Artist.Album.2023.FLAC", metadata.title)
        assertTrue(metadata.description!!.contains("512.0 MB"))
        assertEquals("Music", metadata.siteName)
    }

    @Test
    fun testMagnetMetadataExtraction_SoftwareGame() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:0987654321fedcba&dn=Awesome.Game.v1.2.3.PC.Game.ISO&xl=10737418240"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Awesome.Game.v1.2.3.PC.Game.ISO", metadata.title)
        assertTrue(metadata.description!!.contains("10.0 GB"))
        assertEquals("Software/Game", metadata.siteName)
    }

    @Test
    fun testMagnetMetadataExtraction_BookDocument() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcdef1234567890&dn=Programming.Guide.2023.PDF&xl=104857600"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Programming.Guide.2023.PDF", metadata.title)
        assertTrue(metadata.description!!.contains("100.0 MB"))
        assertEquals("Book/Document", metadata.siteName)
    }

    @Test
    fun testMagnetMetadataExtraction_WithURLEncoding() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:123456789&dn=Movie+Title+With+Spaces%20And%20Special%21%40%23&xl=1048576000"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Movie Title With Spaces And Special!@#", metadata.title)
        assertTrue(metadata.description!!.contains("1000.0 MB"))
    }

    @Test
    fun testMagnetMetadataExtraction_MinimalInfo() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcdef0123456789&dn=SimpleFile"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("SimpleFile", metadata.title)
        assertTrue(metadata.description!!.contains("BitTorrent magnet link"))
        assertTrue(metadata.description!!.contains("abcdef01..."))
        assertEquals("BitTorrent", metadata.siteName)
    }

    @Test
    fun testMagnetMetadataExtraction_NoDisplayName() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:fedcba9876543210&xl=2048000000"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Magnet Link", metadata.title)
        assertTrue(metadata.description!!.contains("1.9 GB"))
        assertTrue(metadata.description!!.contains("fedcba98..."))
        assertEquals("BitTorrent", metadata.siteName)
    }

    @Test
    fun testMagnetMetadataExtraction_OnlyInfoHash() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:1234567890abcdef"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Magnet Link", metadata.title)
        assertTrue(metadata.description!!.contains("BitTorrent magnet link"))
        assertTrue(metadata.description!!.contains("12345678..."))
        assertEquals("BitTorrent", metadata.siteName)
    }

    @Test
    fun testSizeFormatting_Bytes() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234&dn=SmallFile&xl=512"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertTrue(metadata.description!!.contains("512.0 B"))
    }

    @Test
    fun testSizeFormatting_Kilobytes() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234&dn=MediumFile&xl=524288"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertTrue(metadata.description!!.contains("512.0 KB"))
    }

    @Test
    fun testSizeFormatting_Terabytes() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234&dn=HugeFile&xl=1099511627776"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertTrue(metadata.description!!.contains("1.0 TB"))
    }

    @Test
    fun testContentTypeInference_MovieByExtension() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234&dn=Some.Random.Name.mp4"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Movie", metadata.siteName)
    }

    @Test
    fun testContentTypeInference_TVShowBySeason() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234&dn=Random.Show.Season.3.Complete"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("TV Show", metadata.siteName)
    }

    @Test
    fun testContentTypeInference_MusicByKeyword() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234&dn=Artist.Music.Collection.2023"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Music", metadata.siteName)
    }

    @Test
    fun testContentTypeInference_SoftwareByExtension() = runBlocking {
        val magnetUrl = "magnet:?xt=urn:btih:abcd1234&dn=Application.Setup.exe"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Software/Game", metadata.siteName)
    }

    @Test
    fun testMalformedMagnetUrl_ReturnsBasicMetadata() = runBlocking {
        val magnetUrl = "magnet:invalid-format"

        val metadata = metadataFetcher.fetchMetadata(magnetUrl)

        assertEquals("Magnet Link", metadata.title)
        assertEquals("BitTorrent magnet link", metadata.description)
        assertEquals("BitTorrent", metadata.siteName)
    }

    @Test
    fun testTorrentFileUrl_ExtractsFilename() = runBlocking {
        val torrentUrl = "http://example.com/torrents/AwesomeMovie2023.torrent"

        val metadata = metadataFetcher.fetchMetadata(torrentUrl)

        assertEquals("AwesomeMovie2023", metadata.title)
        assertEquals("Torrent file", metadata.description)
        assertEquals("BitTorrent", metadata.siteName)
    }

    @Test
    fun testRegularUrl_FallsBackToGenericHandling() = runBlocking {
        val regularUrl = "http://example.com/some-page"

        // This would normally try to fetch the page, but will fail in unit tests
        // The error handling should provide fallback metadata
        val metadata = metadataFetcher.fetchMetadata(regularUrl)

        assertNotNull(metadata.title)
        assertNotNull(metadata.siteName)
    }
}