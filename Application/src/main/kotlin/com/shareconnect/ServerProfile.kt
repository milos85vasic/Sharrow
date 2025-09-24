package com.shareconnect

import android.content.Context

class ServerProfile {
    var id: String? = null
    var name: String? = null
    var url: String? = null
    var port: Int = 0
    var isDefault: Boolean = false
    var serviceType: String? = TYPE_METUBE // metube, ytdl, torrent, jdownloader
    var torrentClientType: String? = null // qbittorrent, transmission, utorrent (only for torrent type)
    var username: String? = null // Optional authentication field
    var password: String? = null // Optional authentication field

    constructor() {
        // Default constructor required for serialization
        this.serviceType = TYPE_METUBE // Default to MeTube for backward compatibility
    }

    constructor(id: String?, name: String?, url: String?, port: Int, isDefault: Boolean) {
        this.id = id
        this.name = name
        this.url = url
        this.port = port
        this.isDefault = isDefault
        this.serviceType = TYPE_METUBE // Default to MeTube for backward compatibility
    }

    constructor(
        id: String?,
        name: String?,
        url: String?,
        port: Int,
        isDefault: Boolean,
        serviceType: String?,
        torrentClientType: String?
    ) {
        this.id = id
        this.name = name
        this.url = url
        this.port = port
        this.isDefault = isDefault
        this.serviceType = serviceType
        this.torrentClientType = torrentClientType
    }
    
    constructor(
        id: String?,
        name: String?,
        url: String?,
        port: Int,
        isDefault: Boolean,
        serviceType: String?,
        torrentClientType: String?,
        username: String?,
        password: String?
    ) {
        this.id = id
        this.name = name
        this.url = url
        this.port = port
        this.isDefault = isDefault
        this.serviceType = serviceType
        this.torrentClientType = torrentClientType
        this.username = username
        this.password = password
    }

    fun isMeTube(): Boolean {
        return TYPE_METUBE == serviceType
    }

    fun isYtDl(): Boolean {
        return TYPE_YTDL == serviceType
    }

    fun isTorrent(): Boolean {
        return TYPE_TORRENT == serviceType
    }

    fun isJDownloader(): Boolean {
        return TYPE_JDOWNLOADER == serviceType
    }

    fun getServiceTypeName(context: Context): String {
        return when (serviceType) {
            TYPE_METUBE -> context.getString(R.string.metube)
            TYPE_YTDL -> context.getString(R.string.ytdlp)
            TYPE_TORRENT -> context.getString(R.string.torrent) + " (" + getTorrentClientName(context) + ")"
            TYPE_JDOWNLOADER -> context.getString(R.string.jdownloader)
            else -> context.getString(R.string.unknown)
        }
    }

    // Deprecated method for backward compatibility
    fun getServiceTypeName(): String {
        return when (serviceType) {
            TYPE_METUBE -> "MeTube"
            TYPE_YTDL -> "YT-DLP"
            TYPE_TORRENT -> "Torrent (" + getTorrentClientName() + ")"
            TYPE_JDOWNLOADER -> "jDownloader"
            else -> "Unknown"
        }
    }

    fun getTorrentClientName(context: Context): String {
        if (torrentClientType == null) {
            return context.getString(R.string.unknown)
        }

        return when (torrentClientType) {
            TORRENT_CLIENT_QBITTORRENT -> context.getString(R.string.qbittorrent)
            TORRENT_CLIENT_TRANSMISSION -> context.getString(R.string.transmission)
            TORRENT_CLIENTUTORRENT -> context.getString(R.string.utorrent)
            else -> torrentClientType ?: ""
        }
    }

    // Deprecated method for backward compatibility
    fun getTorrentClientName(): String {
        if (torrentClientType == null) {
            return "Unknown"
        }

        return when (torrentClientType) {
            TORRENT_CLIENT_QBITTORRENT -> "qBittorrent"
            TORRENT_CLIENT_TRANSMISSION -> "Transmission"
            TORRENT_CLIENTUTORRENT -> "uTorrent"
            else -> torrentClientType ?: ""
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null || javaClass != obj.javaClass) return false
        val that = obj as ServerProfile
        return port == that.port &&
                isDefault == that.isDefault &&
                id == that.id &&
                name == that.name &&
                url == that.url &&
                serviceType == that.serviceType &&
                torrentClientType == that.torrentClientType &&
                username == that.username &&
                password == that.password
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + port
        result = 31 * result + if (isDefault) 1 else 0
        result = 31 * result + (serviceType?.hashCode() ?: 0)
        result = 31 * result + (torrentClientType?.hashCode() ?: 0)
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (password?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val TYPE_METUBE = "metube"
        const val TYPE_YTDL = "ytdl"
        const val TYPE_TORRENT = "torrent"
        const val TYPE_JDOWNLOADER = "jdownloader"

        const val TORRENT_CLIENT_QBITTORRENT = "qbittorrent"
        const val TORRENT_CLIENT_TRANSMISSION = "transmission"
        const val TORRENT_CLIENTUTORRENT = "utorrent"
    }
}