package com.shareconnect.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_profiles")
class ServerProfileEntity {
    @PrimaryKey
    var id: String = ""

    var name: String? = null
    var url: String? = null
    var port: Int = 0
    var isDefault: Boolean = false
    var serviceType: String? = null
    var torrentClientType: String? = null
    var username: String? = null
    var password: String? = null

    constructor()

    @androidx.room.Ignore
    constructor(
        id: String,
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
}