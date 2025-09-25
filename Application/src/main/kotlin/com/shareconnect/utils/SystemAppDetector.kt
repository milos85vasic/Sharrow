package com.shareconnect.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import com.shareconnect.utils.UrlCompatibilityUtils.UrlType

/**
 * Utility class for detecting system apps that can handle specific URL types
 */
object SystemAppDetector {

    data class AppInfo(
        val packageName: String,
        val appName: String,
        val icon: Drawable,
        val className: String?
    )

    /**
     * Get all apps that can handle the given URL
     */
    fun getCompatibleApps(context: Context, url: String?): List<AppInfo> {
        if (url.isNullOrBlank()) return emptyList()

        val packageManager = context.packageManager
        val compatibleApps = mutableListOf<AppInfo>()

        try {
            val urlType = UrlCompatibilityUtils.detectUrlType(url)

            when (urlType) {
                UrlType.STREAMING -> {
                    // For streaming URLs, find apps that can handle VIEW intents with the URL
                    compatibleApps.addAll(getAppsForViewIntent(context, url))
                    // Also find apps that can handle the specific streaming service
                    compatibleApps.addAll(getStreamingSpecificApps(context, url))
                }
                UrlType.TORRENT -> {
                    // For torrent URLs, find torrent clients and apps that can handle magnet links
                    if (url.startsWith("magnet:", ignoreCase = true)) {
                        compatibleApps.addAll(getAppsForMagnetLinks(context))
                    } else {
                        compatibleApps.addAll(getAppsForTorrentFiles(context))
                    }
                }
                UrlType.DIRECT_DOWNLOAD -> {
                    // For direct downloads, find download managers and browsers
                    compatibleApps.addAll(getAppsForViewIntent(context, url))
                    compatibleApps.addAll(getDownloadManagerApps(context))
                }
                null -> {
                    // Fallback to generic URL handling
                    compatibleApps.addAll(getAppsForViewIntent(context, url))
                }
            }

        } catch (e: Exception) {
            // Fallback to basic URL handling
            compatibleApps.addAll(getAppsForViewIntent(context, url))
        }

        // Remove duplicates and sort by app name
        return compatibleApps
            .distinctBy { it.packageName }
            .sortedBy { it.appName }
    }

    /**
     * Get apps that can handle VIEW intents for the URL
     */
    private fun getAppsForViewIntent(context: Context, url: String): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)

            for (resolveInfo in resolveInfos) {
                if (!isSystemShareDialog(resolveInfo)) {
                    val appInfo = createAppInfo(packageManager, resolveInfo)
                    if (appInfo != null) {
                        apps.add(appInfo)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore malformed URLs
        }

        return apps
    }

    /**
     * Get apps that can handle magnet links
     */
    private fun getAppsForMagnetLinks(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("magnet:?xt=urn:btih:test")
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)

            for (resolveInfo in resolveInfos) {
                if (!isSystemShareDialog(resolveInfo)) {
                    val appInfo = createAppInfo(packageManager, resolveInfo)
                    if (appInfo != null) {
                        apps.add(appInfo)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        return apps
    }

    /**
     * Get apps that can handle torrent files
     */
    private fun getAppsForTorrentFiles(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.type = "application/x-bittorrent"
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)

            for (resolveInfo in resolveInfos) {
                if (!isSystemShareDialog(resolveInfo)) {
                    val appInfo = createAppInfo(packageManager, resolveInfo)
                    if (appInfo != null) {
                        apps.add(appInfo)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        return apps
    }

    /**
     * Get streaming service specific apps (YouTube app for YouTube URLs, etc.)
     */
    private fun getStreamingSpecificApps(context: Context, url: String): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        try {
            when {
                url.contains("youtube.com") || url.contains("youtu.be") -> {
                    // Look for YouTube app
                    apps.addAll(getAppsByPackageName(context, listOf(
                        "com.google.android.youtube",
                        "com.google.android.youtube.tv"
                    )))
                }
                url.contains("vimeo.com") -> {
                    // Look for Vimeo app
                    apps.addAll(getAppsByPackageName(context, listOf("com.vimeo.android.videoapp")))
                }
                url.contains("twitch.tv") -> {
                    // Look for Twitch app
                    apps.addAll(getAppsByPackageName(context, listOf("tv.twitch.android.app")))
                }
                url.contains("soundcloud.com") -> {
                    // Look for SoundCloud app
                    apps.addAll(getAppsByPackageName(context, listOf("com.soundcloud.android")))
                }
                url.contains("reddit.com") -> {
                    // Look for Reddit apps
                    apps.addAll(getAppsByPackageName(context, listOf(
                        "com.reddit.frontpage",
                        "com.laurencedawson.reddit_sync",
                        "com.onelouder.baconreader"
                    )))
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        return apps
    }

    /**
     * Get download manager apps
     */
    private fun getDownloadManagerApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        // Common download manager package names
        val downloadManagers = listOf(
            "com.dv.adm", // Advanced Download Manager
            "idm.internet.download.manager.plus", // Internet Download Manager
            "com.downloadmanager", // Download Manager
            "com.loadr.downloadmanager", // Loadr Download Manager
            "com.farthan.download" // Fast Download Manager
        )

        apps.addAll(getAppsByPackageName(context, downloadManagers))

        return apps
    }

    /**
     * Get apps by package name if they exist
     */
    private fun getAppsByPackageName(context: Context, packageNames: List<String>): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        for (packageName in packageNames) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val icon = packageManager.getApplicationIcon(appInfo)

                apps.add(AppInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    className = null
                ))
            } catch (e: PackageManager.NameNotFoundException) {
                // App not installed
            } catch (e: Exception) {
                // Other errors
            }
        }

        return apps
    }

    /**
     * Create AppInfo from ResolveInfo
     */
    private fun createAppInfo(packageManager: PackageManager, resolveInfo: ResolveInfo): AppInfo? {
        return try {
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val packageName = resolveInfo.activityInfo.packageName
            val className = resolveInfo.activityInfo.name

            AppInfo(
                packageName = packageName,
                appName = appName,
                icon = icon,
                className = className
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if this is a system share dialog (exclude from results)
     */
    private fun isSystemShareDialog(resolveInfo: ResolveInfo): Boolean {
        val packageName = resolveInfo.activityInfo.packageName
        return packageName == "android" ||
               packageName == "com.android.internal.app" ||
               resolveInfo.activityInfo.name.contains("ResolverActivity") ||
               resolveInfo.activityInfo.name.contains("ChooserActivity")
    }

    /**
     * Launch a specific app with the URL
     */
    fun launchApp(context: Context, appInfo: AppInfo, url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setClassName(appInfo.packageName, appInfo.className ?: "")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // If className is not available, use package name only
            if (appInfo.className.isNullOrEmpty()) {
                intent.setPackage(appInfo.packageName)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}