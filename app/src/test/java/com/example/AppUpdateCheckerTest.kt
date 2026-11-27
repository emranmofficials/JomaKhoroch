package com.example

import com.example.infrastructure.AppUpdateChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateCheckerTest {

    @Test
    fun isNewerVersion_handlesSemanticVersionsAccurately() {
        assertTrue(AppUpdateChecker.isNewerVersion("1.0.1", "1.0.0"))
        assertTrue(AppUpdateChecker.isNewerVersion("v1.1.0", "1.0.9"))
        assertTrue(AppUpdateChecker.isNewerVersion("v2.0.0", "1.9.9"))
        assertTrue(AppUpdateChecker.isNewerVersion("1.10.0", "1.2.5"))
        assertTrue(AppUpdateChecker.isNewerVersion("1.0.0.1", "1.0.0"))

        assertFalse(AppUpdateChecker.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(AppUpdateChecker.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(AppUpdateChecker.isNewerVersion("0.9.9", "1.0.0"))
        assertFalse(AppUpdateChecker.isNewerVersion("1.2.0", "1.10.0"))
        assertFalse(AppUpdateChecker.isNewerVersion("", "1.0.0"))
    }

    @Test
    fun normalizeGitHubRepo_handlesSlugAndUrls() {
        assertEquals("emran/jomakhoroch", AppUpdateChecker.normalizeGitHubRepo("emran/jomakhoroch"))
        assertEquals("emran/jomakhoroch", AppUpdateChecker.normalizeGitHubRepo("https://github.com/emran/jomakhoroch"))
        assertEquals("emran/jomakhoroch", AppUpdateChecker.normalizeGitHubRepo("https://github.com/emran/jomakhoroch.git"))
        assertEquals("emran/jomakhoroch", AppUpdateChecker.normalizeGitHubRepo("github.com/emran/jomakhoroch/releases"))
        assertNull(AppUpdateChecker.normalizeGitHubRepo("invalid-single-token"))
        assertNull(AppUpdateChecker.normalizeGitHubRepo(""))
    }

    @Test
    fun selectBestDownloadUrl_prefersReleaseApkAndValidatesHttps() {
        val assets = listOf(
            "JomaKhoroch-v1.2.0-debug.apk" to "https://github.com/user/jomakhoroch/releases/download/v1.2.0/JomaKhoroch-v1.2.0-debug.apk",
            "JomaKhoroch-v1.2.0-release.apk" to "https://github.com/user/jomakhoroch/releases/download/v1.2.0/JomaKhoroch-v1.2.0-release.apk"
        )
        val best = AppUpdateChecker.selectBestDownloadUrl(
            assets = assets,
            releasePageUrl = "https://github.com/user/jomakhoroch/releases/tag/v1.2.0"
        )
        assertEquals(
            "https://github.com/user/jomakhoroch/releases/download/v1.2.0/JomaKhoroch-v1.2.0-release.apk",
            best
        )

        val fallbackToPage = AppUpdateChecker.selectBestDownloadUrl(
            assets = listOf("checksums.txt" to "https://github.com/user/jomakhoroch/releases/download/v1.2.0/SHA256SUMS.txt"),
            releasePageUrl = "https://github.com/user/jomakhoroch/releases/tag/v1.2.0"
        )
        assertEquals("https://github.com/user/jomakhoroch/releases/tag/v1.2.0", fallbackToPage)

        val rejectsInsecureHttp = AppUpdateChecker.selectBestDownloadUrl(
            assets = listOf("app-release.apk" to "http://insecure.example.com/app-release.apk"),
            releasePageUrl = "http://insecure.example.com/release"
        )
        assertNull(rejectsInsecureHttp)
    }
}
