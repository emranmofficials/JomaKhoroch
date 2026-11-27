package com.example.infrastructure

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AppReleaseInfo(
    val tagName: String,
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val releasePageUrl: String,
    val apkDownloadUrl: String,
    val publishedAt: String
)

sealed interface UpdateCheckResult {
    data class UpdateAvailable(val release: AppReleaseInfo) : UpdateCheckResult
    data class UpToDate(val latestVersion: String) : UpdateCheckResult
    data object RepoNotConfigured : UpdateCheckResult
    data object Offline : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}

object AppUpdateChecker {

    private val repoSlugRegex = Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")

    /**
     * Accepts either "owner/repo" or a full GitHub URL like "https://github.com/owner/repo(.git)".
     * Returns normalized "owner/repo" or null if invalid.
     */
    fun normalizeGitHubRepo(rawInput: String): String? {
        val trimmed = rawInput.trim().trimEnd('/')
        if (trimmed.isEmpty()) return null

        val withoutPrefix = trimmed
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removePrefix("github.com/")
            .removePrefix("www.github.com/")
            .removeSuffix(".git")
            .trim('/')

        val parts = withoutPrefix.split('/').filter { it.isNotBlank() }
        if (parts.size < 2) return null
        val slug = "${parts[0]}/${parts[1]}"
        return if (repoSlugRegex.matches(slug)) slug else null
    }

    /**
     * Compares semantic versions like "v1.0.2" vs "1.0.0" or "1.10.0" vs "1.2.9".
     * Returns true strictly when [latest] is newer than [current].
     */
    fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = parseVersionParts(latest)
        val currentParts = parseVersionParts(current)
        if (latestParts.isEmpty() || currentParts.isEmpty()) return false

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l != c) return l > c
        }
        return false
    }

    internal fun cleanVersionName(raw: String): String {
        return raw.trim().removePrefix("v").removePrefix("V").trim()
    }

    private fun parseVersionParts(version: String): List<Int> {
        val cleaned = cleanVersionName(version)
            .substringBefore('-')
            .substringBefore('+')
        if (cleaned.isBlank()) return emptyList()
        return cleaned.split('.').mapNotNull { segment ->
            segment.takeWhile { it.isDigit() }.toIntOrNull()
        }
    }

    internal fun selectBestDownloadUrl(
        assets: List<Pair<String, String>>,
        releasePageUrl: String
    ): String? {
        var preferredReleaseApk: String? = null
        var nonDebugApk: String? = null
        var anyApk: String? = null

        for ((rawName, rawUrl) in assets) {
            val name = rawName.lowercase()
            val downloadUrl = rawUrl.trim()
            if (!downloadUrl.startsWith("https://")) continue
            when {
                name.endsWith("-release.apk") -> {
                    preferredReleaseApk = downloadUrl
                    break
                }
                name.endsWith(".apk") && !name.contains("debug") && nonDebugApk == null -> {
                    nonDebugApk = downloadUrl
                }
                name.endsWith(".apk") && anyApk == null -> {
                    anyApk = downloadUrl
                }
            }
        }

        val finalUrl = preferredReleaseApk ?: nonDebugApk ?: anyApk ?: releasePageUrl.trim()
        return finalUrl.takeIf { it.startsWith("https://") }
    }

    internal fun parseReleaseJson(jsonBody: String): AppReleaseInfo? {
        val root = JSONObject(jsonBody)
        val tagName = root.optString("tag_name").trim()
        if (tagName.isBlank()) return null

        val versionName = cleanVersionName(tagName)
        val releaseTitle = root.optString("name").trim().ifBlank { "JomaKhoroch $tagName" }
        val releaseNotes = root.optString("body").trim()
        val releasePageUrl = root.optString("html_url").trim()
        val publishedAt = root.optString("published_at").trim()

        val assetPairs = mutableListOf<Pair<String, String>>()
        val assets = root.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                assetPairs.add(
                    asset.optString("name") to asset.optString("browser_download_url")
                )
            }
        }

        val finalDownloadUrl = selectBestDownloadUrl(assetPairs, releasePageUrl) ?: return null

        return AppReleaseInfo(
            tagName = tagName,
            versionName = versionName,
            releaseTitle = releaseTitle,
            releaseNotes = releaseNotes,
            releasePageUrl = releasePageUrl.ifBlank { finalDownloadUrl },
            apkDownloadUrl = finalDownloadUrl,
            publishedAt = publishedAt
        )
    }

    suspend fun checkLatestGitHubRelease(
        repoInput: String,
        currentVersion: String
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val slug = normalizeGitHubRepo(repoInput) ?: return@withContext UpdateCheckResult.RepoNotConfigured
        val endpoint = "https://api.github.com/repos/$slug/releases/latest"

        var connection: HttpURLConnection? = null
        try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "JomaKhoroch-Android/$currentVersion")
            }

            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    val release = parseReleaseJson(body)
                        ?: return@withContext UpdateCheckResult.Error("Invalid release metadata")
                    if (isNewerVersion(release.versionName, currentVersion)) {
                        UpdateCheckResult.UpdateAvailable(release)
                    } else {
                        UpdateCheckResult.UpToDate(release.versionName)
                    }
                }
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    UpdateCheckResult.Error("NO_RELEASE_FOUND")
                }
                403 -> {
                    UpdateCheckResult.Error("RATE_LIMITED")
                }
                else -> {
                    UpdateCheckResult.Error("HTTP_$responseCode")
                }
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.localizedMessage ?: "NETWORK_ERROR")
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Google Play policy-compliant update handoff:
     * Opens the official HTTPS release or APK download URL in the system browser/downloader
     * without declaring REQUEST_INSTALL_PACKAGES or downloading/executing binaries inside the app.
     */
    fun openUpdateInBrowser(context: Context, url: String): Boolean {
        val sanitized = url.trim()
        if (!sanitized.startsWith("https://")) return false
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sanitized)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
