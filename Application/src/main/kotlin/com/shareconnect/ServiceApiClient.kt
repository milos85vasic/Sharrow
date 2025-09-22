package com.shareconnect

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ServiceApiClient {
    private val client: OkHttpClient

    interface ServiceApiCallback {
        fun onSuccess()
        fun onError(error: String?)
    }

    init {
        client = OkHttpClient()
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
            val request: Request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to send URL to MeTube", e)
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Log.e(TAG, "MeTube API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing MeTube API request", e)
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
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to send URL to YT-DLP", e)
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Log.e(TAG, "YT-DLP API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing YT-DLP API request", e)
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
            Log.e(TAG, "Error preparing torrent client request", e)
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
                    sendToQBittorrent(apiUrl, url, callback)
                }
                ServerProfile.TORRENT_CLIENT_TRANSMISSION -> {
                    // Transmission RPC API
                    apiUrl = baseUrl + "/transmission/rpc"
                    sendToTransmission(apiUrl, url, callback)
                }
                ServerProfile.TORRENT_CLIENTUTORRENT -> {
                    // uTorrent Web API
                    apiUrl = baseUrl + "/gui/"
                    sendToUTorrent(apiUrl, url, callback)
                }
                else -> callback.onError("Unsupported torrent client: " + profile.torrentClientType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending URL to torrent client", e)
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
                    sendToQBittorrent(apiUrl, magnetUrl, callback)
                }
                ServerProfile.TORRENT_CLIENT_TRANSMISSION -> {
                    // Transmission RPC API
                    apiUrl = baseUrl + "/transmission/rpc"
                    sendToTransmission(apiUrl, magnetUrl, callback)
                }
                ServerProfile.TORRENT_CLIENTUTORRENT -> {
                    // uTorrent Web API
                    apiUrl = baseUrl + "/gui/"
                    sendToUTorrent(apiUrl, magnetUrl, callback)
                }
                else -> callback.onError("Unsupported torrent client: " + profile.torrentClientType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending magnet to torrent client", e)
            callback.onError(e.message)
        }
    }

    /**
     * Send to qBittorrent
     */
    private fun sendToQBittorrent(apiUrl: String, url: String, callback: ServiceApiCallback) {
        try {
            // qBittorrent expects form data
            val formBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("urls", url)
                .build()

            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to send to qBittorrent", e)
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Log.e(TAG, "qBittorrent API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing qBittorrent request", e)
            callback.onError(e.message)
        }
    }

    /**
     * Send to Transmission
     */
    private fun sendToTransmission(apiUrl: String, url: String, callback: ServiceApiCallback) {
        try {
            // Transmission expects JSON-RPC
            val json = JSONObject()
            json.put("method", "torrent-add")
            val params = JSONObject()

            // Transmission can handle both URLs and magnet links
            params.put("filename", url)

            json.put("arguments", params)

            val body: RequestBody = json.toString().toRequestBody(JSON)
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("X-Transmission-Session-Id", "dummy") // May need to handle session ID properly
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to send to Transmission", e)
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Log.e(TAG, "Transmission API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing Transmission request", e)
            callback.onError(e.message)
        }
    }

    /**
     * Send to uTorrent
     */
    private fun sendToUTorrent(apiUrl: String, url: String, callback: ServiceApiCallback) {
        try {
            // uTorrent Web API - this is a simplified version
            val fullUrl = "$apiUrl?action=add-url&s=$url"

            val request: Request = Request.Builder()
                .url(fullUrl)
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to send to uTorrent", e)
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Log.e(TAG, "uTorrent API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing uTorrent request", e)
            callback.onError(e.message)
        }
    }

    /**
     * Send URL to jDownloader
     */
    private fun sendUrlToJDownloader(profile: ServerProfile, url: String, callback: ServiceApiCallback) {
        try {
            // jDownloader My.JDownloader API or direct API
            val baseUrl = profile.url + ":" + profile.port
            val apiUrl = baseUrl + "/flashget" // Common jDownloader endpoint for direct downloads

            // jDownloader typically accepts GET requests with URL parameters
            val fullUrl = "$apiUrl?url=$url"

            val request: Request = Request.Builder()
                .url(fullUrl)
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to send to jDownloader", e)
                    callback.onError(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            callback.onSuccess()
                        } else {
                            val errorBody = if (response.body != null) response.body!!.string() else "Unknown error"
                            Log.e(TAG, "jDownloader API error: $errorBody")
                            callback.onError("API Error: " + response.code + " - $errorBody")
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing jDownloader request", e)
            callback.onError(e.message)
        }
    }

    companion object {
        private const val TAG = "ServiceApiClient"
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }
}