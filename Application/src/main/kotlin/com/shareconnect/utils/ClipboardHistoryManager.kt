package com.shareconnect.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ClipboardItem(
    val text: String,
    val timestamp: Long,
    val isUrl: Boolean = false
)

class ClipboardHistoryManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "clipboard_history"
        private const val HISTORY_KEY = "clipboard_items"
        private const val MAX_HISTORY_SIZE = 10

        @Volatile
        private var INSTANCE: ClipboardHistoryManager? = null

        fun getInstance(context: Context): ClipboardHistoryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ClipboardHistoryManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private var clipboardHistory = mutableListOf<ClipboardItem>()
    private var lastKnownClipText: String? = null

    init {
        loadHistory()
        startMonitoring()
    }

    private fun loadHistory() {
        val historyJson = prefs.getString(HISTORY_KEY, "[]")
        val type = object : TypeToken<MutableList<ClipboardItem>>() {}.type
        clipboardHistory = gson.fromJson(historyJson, type) ?: mutableListOf()
    }

    private fun saveHistory() {
        val historyJson = gson.toJson(clipboardHistory)
        prefs.edit().putString(HISTORY_KEY, historyJson).apply()
    }

    private fun startMonitoring() {
        clipboardManager.addPrimaryClipChangedListener {
            checkClipboardChange()
        }

        // Check initial clipboard content
        checkClipboardChange()
    }

    private fun checkClipboardChange() {
        if (clipboardManager.hasPrimaryClip()) {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val newClipText = clipData.getItemAt(0).text?.toString()

                if (newClipText != null && newClipText != lastKnownClipText) {
                    lastKnownClipText = newClipText
                    addToHistory(newClipText.trim())
                }
            }
        }
    }

    private fun addToHistory(text: String) {
        // Don't add empty strings or duplicates
        if (text.isEmpty() || clipboardHistory.any { it.text == text }) {
            return
        }

        val clipboardItem = ClipboardItem(
            text = text,
            timestamp = System.currentTimeMillis(),
            isUrl = isValidUrl(text)
        )

        // Add to beginning of list
        clipboardHistory.add(0, clipboardItem)

        // Keep only the most recent items
        if (clipboardHistory.size > MAX_HISTORY_SIZE) {
            clipboardHistory = clipboardHistory.take(MAX_HISTORY_SIZE).toMutableList()
        }

        saveHistory()
    }

    fun getClipboardHistory(): List<ClipboardItem> {
        // Also check current clipboard in case we missed something
        checkClipboardChange()
        return clipboardHistory.toList()
    }

    fun getUrlItems(): List<ClipboardItem> {
        return getClipboardHistory().filter { it.isUrl }
    }

    fun getCurrentClipboardText(): String? {
        return if (clipboardManager.hasPrimaryClip()) {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                clipData.getItemAt(0).text?.toString()
            } else null
        } else null
    }

    fun clearHistory() {
        clipboardHistory.clear()
        saveHistory()
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val urlPattern = Regex(
                "^(https?://)" +
                "([\\da-z\\.-]+)" +
                "\\.([a-z\\.]{2,6})" +
                "([/\\w \\.-]*)*" +
                "/?$"
            )
            url.matches(urlPattern) ||
            url.contains("youtube.com") ||
            url.contains("youtu.be") ||
            url.contains("vimeo.com") ||
            url.contains("twitch.tv") ||
            url.contains("reddit.com") ||
            url.contains("twitter.com") ||
            url.contains("instagram.com") ||
            url.contains("facebook.com") ||
            url.contains("soundcloud.com") ||
            url.contains("tiktok.com") ||
            url.startsWith("magnet:")
        } catch (e: Exception) {
            false
        }
    }
}