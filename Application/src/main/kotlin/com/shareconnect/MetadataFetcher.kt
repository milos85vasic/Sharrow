package com.shareconnect

import com.redelf.commons.logging.Console
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import java.util.concurrent.TimeUnit

data class UrlMetadata(
    val title: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val siteName: String?
)

class MetadataFetcher {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchMetadata(url: String): UrlMetadata = withContext(Dispatchers.IO) {
        try {
            // Special handling for known services
            when {
                isYouTubeUrl(url) -> fetchYouTubeMetadata(url)
                isVimeoUrl(url) -> fetchVimeoMetadata(url)
                isTwitchUrl(url) -> fetchTwitchMetadata(url)
                isRedditUrl(url) -> fetchRedditMetadata(url)
                isTwitterUrl(url) -> fetchTwitterMetadata(url)
                isInstagramUrl(url) -> fetchInstagramMetadata(url)
                isFacebookUrl(url) -> fetchFacebookMetadata(url)
                isSoundCloudUrl(url) -> fetchSoundCloudMetadata(url)
                isTikTokUrl(url) -> fetchTikTokMetadata(url)
                isTorrentOrMagnet(url) -> handleTorrentMetadata(url)
                else -> fetchGenericMetadata(url)
            }
        } catch (e: Exception) {
            Console.error(e, "Error fetching metadata for URL: $url")
            UrlMetadata(
                title = extractTitleFromUrl(url),
                description = null,
                thumbnailUrl = null,
                siteName = extractSiteNameFromUrl(url)
            )
        }
    }

    private fun fetchGenericMetadata(url: String): UrlMetadata {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", USER_AGENT)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch URL: ${response.code}")
            }

            val html = response.body?.string() ?: throw Exception("Empty response body")
            val doc = Jsoup.parse(html, url)

            return extractMetadata(doc)
        }
    }

    private fun extractMetadata(doc: Document): UrlMetadata {
        // Try Open Graph tags first
        val ogTitle = doc.select("meta[property=og:title]").attr("content")
        val ogDescription = doc.select("meta[property=og:description]").attr("content")
        val ogImage = doc.select("meta[property=og:image]").attr("content")
        val ogSiteName = doc.select("meta[property=og:site_name]").attr("content")

        // Fall back to Twitter Card tags
        val twitterTitle = doc.select("meta[name=twitter:title]").attr("content")
        val twitterDescription = doc.select("meta[name=twitter:description]").attr("content")
        val twitterImage = doc.select("meta[name=twitter:image]").attr("content")

        // Fall back to standard meta tags and title
        val metaDescription = doc.select("meta[name=description]").attr("content")
        val pageTitle = doc.title()

        return UrlMetadata(
            title = ogTitle.ifEmpty { twitterTitle.ifEmpty { pageTitle } },
            description = ogDescription.ifEmpty { twitterDescription.ifEmpty { metaDescription } },
            thumbnailUrl = ogImage.ifEmpty { twitterImage },
            siteName = ogSiteName
        )
    }

    private fun fetchYouTubeMetadata(url: String): UrlMetadata {
        // For YouTube, we can use the standard approach as they provide good Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun fetchVimeoMetadata(url: String): UrlMetadata {
        // Vimeo provides good Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun fetchTwitchMetadata(url: String): UrlMetadata {
        // Twitch provides Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun fetchRedditMetadata(url: String): UrlMetadata {
        // Reddit provides Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun fetchTwitterMetadata(url: String): UrlMetadata {
        // Twitter/X provides Twitter Card tags
        return fetchGenericMetadata(url)
    }

    private fun fetchInstagramMetadata(url: String): UrlMetadata {
        // Instagram provides Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun fetchFacebookMetadata(url: String): UrlMetadata {
        // Facebook provides Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun fetchSoundCloudMetadata(url: String): UrlMetadata {
        // SoundCloud provides Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun fetchTikTokMetadata(url: String): UrlMetadata {
        // TikTok provides Open Graph tags
        return fetchGenericMetadata(url)
    }

    private fun handleTorrentMetadata(url: String): UrlMetadata {
        return if (url.startsWith("magnet:")) {
            // Extract name from magnet link
            val name = extractNameFromMagnet(url)
            UrlMetadata(
                title = name ?: "Magnet Link",
                description = "Torrent magnet link",
                thumbnailUrl = null,
                siteName = "BitTorrent"
            )
        } else {
            // For torrent files, just use the filename
            val filename = url.substringAfterLast("/").substringBeforeLast(".")
            UrlMetadata(
                title = filename,
                description = "Torrent file",
                thumbnailUrl = null,
                siteName = "BitTorrent"
            )
        }
    }

    private fun extractNameFromMagnet(magnetUrl: String): String? {
        // Try to extract display name from magnet link
        val dnPattern = Regex("dn=([^&]+)")
        val match = dnPattern.find(magnetUrl)
        return match?.groupValues?.getOrNull(1)?.replace("+", " ")
    }

    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    private fun isVimeoUrl(url: String): Boolean {
        return url.contains("vimeo.com")
    }

    private fun isTwitchUrl(url: String): Boolean {
        return url.contains("twitch.tv")
    }

    private fun isRedditUrl(url: String): Boolean {
        return url.contains("reddit.com") || url.contains("redd.it")
    }

    private fun isTwitterUrl(url: String): Boolean {
        return url.contains("twitter.com") || url.contains("x.com")
    }

    private fun isInstagramUrl(url: String): Boolean {
        return url.contains("instagram.com")
    }

    private fun isFacebookUrl(url: String): Boolean {
        return url.contains("facebook.com") || url.contains("fb.com")
    }

    private fun isSoundCloudUrl(url: String): Boolean {
        return url.contains("soundcloud.com")
    }

    private fun isTikTokUrl(url: String): Boolean {
        return url.contains("tiktok.com")
    }

    private fun isTorrentOrMagnet(url: String): Boolean {
        return url.startsWith("magnet:") || url.endsWith(".torrent")
    }

    private fun extractTitleFromUrl(url: String): String {
        return try {
            val urlObj = URL(url)
            urlObj.path.substringAfterLast("/").ifEmpty { urlObj.host }
        } catch (e: Exception) {
            url.substringAfterLast("/")
        }
    }

    private fun extractSiteNameFromUrl(url: String): String? {
        return try {
            val urlObj = URL(url)
            urlObj.host.replace("www.", "")
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
}