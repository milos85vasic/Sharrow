package com.shareconnect

import com.redelf.commons.logging.Console
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.FormBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class ServiceApiClient {
    private val client: OkHttpClient
    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()

    interface ServiceApiCallback {
        fun onSuccess()
        fun onError(error: String?)
    }

    init {
        // Initialize client with cookie support for proper authentication handling
        client = OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: ArrayList()
                }
            })
            .build()
    }

    /**
     * Send URL to the appropriate service based on profile type
     */
    fun sendUrlToService(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        when (profile.serviceType) {
            ServerProfile.TYPE_METUBE -> sendUrlToMeTube(profile, url, callback)
            ServerProfile.TYPE_YTDL -> sendUrlToYtdl(profile, url, callback)
            ServerProfile.TYPE_TORRENT -> sendUrlToTorrentClient(profile, url, callback)
            ServerProfile.TYPE_JDOWNLOADER -> sendUrlToJDownloader(profile, url, callback)
            else -> callback.onError("Unsupported service type: " + profile.serviceType)
        }
    }

    /**
     * Send URL to MeTube service
     */
    private fun sendUrlToMeTube(profile: ServerProfile, youtubeUrl: String, callback: ServiceApiCallback) {
        try {
            // Construct the MeTube API endpoint
            val url = profile.url + ":" + profile.port + "/add"

            // Create the JSON payload
            val json = JSONObject()
            json.put("url", youtubeUrl)
            json.put("quality", "best") // Default to best quality

            val body: RequestBody = json.toString().toRequestBody(JSON)
            val requestBuilder = Request.Builder().url(url).post(body)
            
            // Add authentication if provided
            addAuthentication(requestBuilder, profile)
            
            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to send URL to MeTube")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("MeTube API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing MeTube API request")
            callback.onError(e.message)
        }
    }

    /**
     * Send URL to YT-DLP service
     */
    private fun sendUrlToYtdl(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        try {
            // Construct the YT-DLP API endpoint (similar to MeTube)
            val apiUrl = profile.url + ":" + profile.port + "/add"

            // Create the JSON payload
            val json = JSONObject()
            json.put("url", url)
            json.put("quality", "best") // Default to best quality

            val body: RequestBody = json.toString().toRequestBody(JSON)
            val requestBuilder = Request.Builder().url(apiUrl).post(body)
            
            // Add authentication if provided
            addAuthentication(requestBuilder, profile)
            
            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to send URL to YT-DLP")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("YT-DLP API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing YT-DLP API request")
            callback.onError(e.message)
        }
    }

    /**
     * Send URL to torrent client
     */
    private fun sendUrlToTorrentClient(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        try {
            val baseUrl = profile.url + ":" + profile.port

            // Check if it's a magnet link or regular URL
            if (url.startsWith("magnet:")) {
                // For magnet links, send directly to torrent client
                sendMagnetToTorrentClient(profile, url, callback)
            } else {
                // For regular URLs, we might need to handle differently depending on the client
                sendRegularUrlToTorrentClient(profile, url, callback)
            }
        } catch (e: Exception) {
            Console.error(e, "Error preparing torrent client request")
            callback.onError(e.message)
        }
    }

    /**
     * Send regular URL to torrent client
     */
    private fun sendRegularUrlToTorrentClient(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        try {
            val baseUrl = profile.url + ":" + profile.port
            val apiUrl: String

            // Different torrent clients have different APIs
            when (profile.torrentClientType) {
                ServerProfile.TORRENT_CLIENT_QBITTORRENT -> {
                    // qBittorrent Web API
                    apiUrl = baseUrl + "/api/v2/torrents/add"
                    sendToQBittorrent(profile, apiUrl, url, callback)
                }
                ServerProfile.TORRENT_CLIENT_TRANSMISSION -> {
                    // Transmission RPC API
                    apiUrl = baseUrl + "/transmission/rpc"
                    sendToTransmission(profile, apiUrl, url, callback)
                }
                ServerProfile.TORRENT_CLIENTUTORRENT -> {
                    // uTorrent Web API
                    apiUrl = baseUrl + "/gui/"
                    sendToUTorrent(profile, apiUrl, url, callback)
                }
                else -> callback.onError("Unsupported torrent client: " + profile.torrentClientType)
            }
        } catch (e: Exception) {
            Console.error(e, "Error sending URL to torrent client")
            callback.onError(e.message)
        }
    }

    /**
     * Send magnet link to torrent client
     */
    private fun sendMagnetToTorrentClient(profile: ServerProfile, magnetUrl: String, callback: ServiceApiCallback) {
        try {
            val baseUrl = profile.url + ":" + profile.port
            val apiUrl: String

            // Different torrent clients have different APIs
            when (profile.torrentClientType) {
                ServerProfile.TORRENT_CLIENT_QBITTORRENT -> {
                    // qBittorrent Web API
                    apiUrl = baseUrl + "/api/v2/torrents/add"
                    sendToQBittorrent(profile, apiUrl, magnetUrl, callback)
                }
                ServerProfile.TORRENT_CLIENT_TRANSMISSION -> {
                    // Transmission RPC API
                    apiUrl = baseUrl + "/transmission/rpc"
                    sendToTransmission(profile, apiUrl, magnetUrl, callback)
                }
                ServerProfile.TORRENT_CLIENTUTORRENT -> {
                    // uTorrent Web API
                    apiUrl = baseUrl + "/gui/"
                    sendToUTorrent(profile, apiUrl, magnetUrl, callback)
                }
                else -> callback.onError("Unsupported torrent client: " + profile.torrentClientType)
            }
        } catch (e: Exception) {
            Console.error(e, "Error sending magnet to torrent client")
            callback.onError(e.message)
        }
    }

    /**
     * Send to qBittorrent
     */
    private fun sendToQBittorrent(profile: ServerProfile, apiUrl: String, url: String, callback: ServiceApiCallback) {
        // qBittorrent requires login first to get a cookie
        if (!profile.username.isNullOrEmpty() && !profile.password.isNullOrEmpty()) {
            loginToQBittorrent(profile, object : ServiceApiCallback {
                override fun onSuccess() {
                    // Now send the actual torrent/magnet URL
                    sendToQBittorrentAuthenticated(profile, apiUrl, url, callback)
                }

                override fun onError(error: String?) {
                    callback.onError("qBittorrent authentication failed: $error")
                }
            })
        } else {
            // No authentication needed, send directly
            sendToQBittorrentAuthenticated(profile, apiUrl, url, callback)
        }
    }

    /**
     * Login to qBittorrent to get authentication cookie
     */
    private fun loginToQBittorrent(profile: ServerProfile, callback: ServiceApiCallback) {
        try {
            val loginUrl = "${profile.url}:${profile.port}/api/v2/auth/login"

            val formBody = FormBody.Builder()
                .add("username", profile.username!!)
                .add("password", profile.password!!)
                .build()

            val request = Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to login to qBittorrent")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            if (responseBody.contains("Ok.") || responseBody.contains("ok", true)) {
                                callback.onSuccess()
                            } else if (responseBody.contains("Fails.", true)) {
                                callback.onError("Invalid username or password")
                            } else {
                                // Some versions just return 200 with empty body on success
                                callback.onSuccess()
                            }
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("qBittorrent login error: $errorBody")
                            callback.onError("Login failed: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error logging into qBittorrent")
            callback.onError(e.message)
        }
    }

    /**
     * Send to qBittorrent after authentication
     */
    private fun sendToQBittorrentAuthenticated(profile: ServerProfile, apiUrl: String, url: String, callback: ServiceApiCallback) {
        try {
            // qBittorrent expects form data
            val formBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("urls", url)
                .addFormDataPart("autoTMM", "false")
                .addFormDataPart("savepath", "")
                .addFormDataPart("cookie", "")
                .addFormDataPart("category", "")
                .addFormDataPart("tags", "")
                .addFormDataPart("skip_checking", "false")
                .addFormDataPart("paused", "false")
                .addFormDataPart("root_folder", "true")
                .build()

            val request = Request.Builder()
                .url(apiUrl)
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to send to qBittorrent")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("qBittorrent API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing qBittorrent request")
            callback.onError(e.message)
        }
    }

    /**
     * Send to Transmission
     */
    private fun sendToTransmission(profile: ServerProfile, apiUrl: String, url: String, callback: ServiceApiCallback) {
        sendToTransmissionWithSessionId(profile, apiUrl, url, null, callback)
    }

    /**
     * Send to Transmission with session ID handling
     */
    private fun sendToTransmissionWithSessionId(
        profile: ServerProfile,
        apiUrl: String,
        url: String,
        sessionId: String?,
        callback: ServiceApiCallback
    ) {
        try {
            // Transmission expects JSON-RPC
            val json = JSONObject()
            json.put("method", "torrent-add")
            val params = JSONObject()

            // Transmission can handle both URLs and magnet links
            params.put("filename", url)

            json.put("arguments", params)

            val body: RequestBody = json.toString().toRequestBody(JSON)
            val requestBuilder = Request.Builder().url(apiUrl).post(body)

            // Add session ID if provided (required by Transmission)
            if (sessionId != null) {
                requestBuilder.addHeader("X-Transmission-Session-Id", sessionId)
            }

            // Add authentication if provided
            addAuthentication(requestBuilder, profile)

            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to send to Transmission")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.code == 409) {
                            // Need to get session ID and retry
                            val newSessionId = response.header("X-Transmission-Session-Id")
                            if (newSessionId != null) {
                                // Retry with the session ID
                                sendToTransmissionWithSessionId(profile, apiUrl, url, newSessionId, callback)
                            } else {
                                callback.onError("Failed to get Transmission session ID")
                            }
                        } else if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            val responseJson = JSONObject(responseBody)
                            val result = responseJson.optString("result", "")
                            if (result == "success") {
                                callback.onSuccess()
                            } else {
                                callback.onError("Transmission error: $result")
                            }
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("Transmission API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing Transmission request")
            callback.onError(e.message)
        }
    }

    /**
     * Send to uTorrent
     */
    private fun sendToUTorrent(profile: ServerProfile, apiUrl: String, url: String, callback: ServiceApiCallback) {
        try {
            // uTorrent Web API - improved implementation with proper token handling
            // First, we need to get a token from /token.html
            val tokenUrl = "${profile.url}:${profile.port}/gui/token.html"
            
            val tokenRequestBuilder = Request.Builder().url(tokenUrl).get()
            // Add authentication for token request
            addAuthentication(tokenRequestBuilder, profile)
            
            val tokenRequest = tokenRequestBuilder.build()

            client.newCall(tokenRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to get uTorrent token")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            // Extract token from response (simplified - in practice would parse HTML)
                            val token = extractTokenFromResponse(responseBody ?: "")
                            
                            // Now send the URL with the token
                            sendUrlToUTorrentWithToken(profile, url, token, callback)
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("uTorrent token error: $errorBody")
                            callback.onError("Token Error: " + response.code + " - $errorBody")
                        }
                    } catch (e: Exception) {
                        Console.error(e, "Error processing uTorrent token response")
                        callback.onError(e.message)
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing uTorrent request")
            callback.onError(e.message)
        }
    }

    /**
     * Send URL to uTorrent with token
     */
    private fun sendUrlToUTorrentWithToken(profile: ServerProfile, url: String, token: String, callback: ServiceApiCallback) {
        try {
            val baseUrl = "${profile.url}:${profile.port}"
            // uTorrent Web API - add URL with token
            val fullUrl = "$baseUrl/gui/?action=add-url&s=$url&token=$token"

            val requestBuilder = Request.Builder().url(fullUrl).get()
            // Add authentication for the actual request
            addAuthentication(requestBuilder, profile)
            
            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to send to uTorrent")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("uTorrent API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error sending URL to uTorrent")
            callback.onError(e.message)
        }
    }

    /**
     * Extract token from uTorrent token response
     * Simplified implementation - in practice would parse HTML properly
     */
    private fun extractTokenFromResponse(response: String): String {
        // Simplified token extraction - in a real implementation, 
        // this would properly parse the HTML to extract the token
        val tokenRegex = "<div id='token' style='display:none;'>([^<]+)</div>".toRegex()
        val matchResult = tokenRegex.find(response)
        return matchResult?.groupValues?.getOrNull(1) ?: ""
    }

    /**
     * Send URL to jDownloader
     */
    private fun sendUrlToJDownloader(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        try {
            // Try My.JDownloader API first (modern approach)
            sendToMyJDownloader(profile, url, object : ServiceApiCallback {
                override fun onSuccess() {
                    callback.onSuccess()
                }
                
                override fun onError(error: String?) {
                    // If My.JDownloader fails, fall back to legacy direct API
                    sendToLegacyJDownloader(profile, url, callback)
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing jDownloader request")
            callback.onError(e.message)
        }
    }

    /**
     * Send URL to My.JDownloader API (modern approach)
     */
    private fun sendToMyJDownloader(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        try {
            val baseUrl = "${profile.url}:${profile.port}"
            // My.JDownloader API endpoint for adding links
            val apiUrl = "$baseUrl/api/linkgrabberv2/addLinks"

            // Prepare JSON payload for My.JDownloader
            val json = JSONObject()
            val links = JSONObject()
            links.put("autostart", true)
            links.put("links", url)
            links.put("packageName", "ShareConnect")
            links.put("extractPassword", "")
            links.put("priority", "DEFAULT")
            links.put("downloadPassword", "")
            links.put("destinationFolder", "")
            links.put("overwritePackagizerRules", false)
            json.put("params", links)
            json.put("apiVer", 1)

            val body: RequestBody = json.toString().toRequestBody(JSON)

            val requestBuilder = Request.Builder().url(apiUrl).post(body)
            requestBuilder.addHeader("Content-Type", "application/json")

            // Add authentication if provided
            addAuthentication(requestBuilder, profile)

            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to send to My.JDownloader")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            // Check if the response indicates success
                            if (responseBody.contains("\"id\":") || responseBody.isEmpty()) {
                                callback.onSuccess()
                            } else {
                                callback.onError("JDownloader response: $responseBody")
                            }
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("My.JDownloader API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing My.JDownloader request")
            callback.onError(e.message)
        }
    }

    /**
     * Send URL to legacy jDownloader direct API (fallback approach)
     */
    private fun sendToLegacyJDownloader(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        try {
            val baseUrl = "${profile.url}:${profile.port}"
            // Legacy jDownloader direct API endpoint
            val apiUrl = "$baseUrl/flashget" // Common jDownloader endpoint for direct downloads

            // jDownloader typically accepts GET requests with URL parameters
            val fullUrl = "$apiUrl?url=$url"

            val requestBuilder = Request.Builder().url(fullUrl).get()
            
            // Add authentication if provided
            addAuthentication(requestBuilder, profile)
            
            val request = requestBuilder.build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Console.error(e, "Failed to send to legacy jDownloader")
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Console.error("Legacy jDownloader API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Console.error(e, "Error preparing legacy jDownloader request")
            callback.onError(e.message)
        }
    }

    /**
     * Helper method to add authentication to a request builder
     */
    private fun addAuthentication(requestBuilder: Request.Builder, profile: ServerProfile) {
        if (!profile.username.isNullOrEmpty() && !profile.password.isNullOrEmpty()) {
            // Basic authentication
            val credentials = okhttp3.Credentials.basic(profile.username!!, profile.password!!)
            requestBuilder.addHeader("Authorization", credentials)
        }
    }

    companion object {
        private const val TAG = "ServiceApiClient"
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }
}