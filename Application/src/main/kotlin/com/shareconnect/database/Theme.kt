package com.shareconnect.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "themes")
class Theme {
    @PrimaryKey
    var id: Int = 0

    var name: String? = null
    var colorScheme: String? = null
    var isDarkMode: Boolean = false
    var isDefault: Boolean = false

    constructor()

    @androidx.room.Ignore
    constructor(id: Int, name: String?, colorScheme: String?, isDarkMode: Boolean, isDefault: Boolean) {
        this.id = id
        this.name = name
        this.colorScheme = colorScheme
        this.isDarkMode = isDarkMode
        this.isDefault = isDefault
    }
}