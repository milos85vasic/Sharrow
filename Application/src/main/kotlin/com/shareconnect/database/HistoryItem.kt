package com.shareconnect.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "history_items")
@TypeConverters(Converters::class)
class HistoryItem {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var url: String? = null
    var title: String? = null
    var serviceProvider: String? = null
    var type: String? = null // single_video, playlist, channel
    var timestamp: Long = 0
    var profileId: String? = null
    var profileName: String? = null
    var isSentSuccessfully: Boolean = false
    var serviceType: String? = null // MeTube, Torrent, jDownloader

    constructor()

    @androidx.room.Ignore
    constructor(
        url: String?,
        title: String?,
        serviceProvider: String?,
        type: String?,
        timestamp: Long,
        profileId: String?,
        profileName: String?,
        isSentSuccessfully: Boolean,
        serviceType: String?
    ) {
        this.url = url
        this.title = title
        this.serviceProvider = serviceProvider
        this.type = type
        this.timestamp = timestamp
        this.profileId = profileId
        this.profileName = profileName
        this.isSentSuccessfully = isSentSuccessfully
        this.serviceType = serviceType
    }
}