package com.xray.core.rust.client.xcra.handler

import android.os.Build
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.BuildConfig
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.CheckUpdateResult
import com.xray.core.rust.client.xcra.dto.GitHubRelease
import com.xray.core.rust.client.xcra.extension.concatUrl
import com.xray.core.rust.client.xcra.util.HttpUtil
import com.xray.core.rust.client.xcra.util.JsonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppUpdateHandler {
    suspend fun checkForUpdate(includePreRelease: Boolean = false): CheckUpdateResult =
        withContext(Dispatchers.IO) {
            val url = if (includePreRelease) {
                AppConfig.APP_API_URL
            } else {
                AppConfig.APP_API_URL.concatUrl("latest")
            }

            var response = HttpUtil.getUrlContent(url, 5000)
            if (response.isNullOrEmpty()) {
                throw IllegalStateException("Failed to get response")
            }
            val latestRelease = if (includePreRelease) {
                JsonUtil.fromJson(response, Array<GitHubRelease>::class.java)
                    .firstOrNull()
                    ?: throw IllegalStateException("No pre-release found")
            } else {
                JsonUtil.fromJson(response, GitHubRelease::class.java)
            }

            val latestVersion = latestRelease.tagName.removePrefix("v")
            App.log(
                "Found new version: $latestVersion (current: ${BuildConfig.VERSION_NAME})"
            )

            return@withContext if (compareVersions(latestVersion, BuildConfig.VERSION_NAME) > 0) {
                val downloadUrl = getDownloadUrl(latestRelease, Build.SUPPORTED_ABIS[0])
                CheckUpdateResult(
                    hasUpdate = true,
                    latestVersion = latestVersion,
                    releaseNotes = latestRelease.body,
                    downloadUrl = downloadUrl,
                    isPreRelease = latestRelease.prerelease
                )
            } else {
                CheckUpdateResult(hasUpdate = false)
            }
        }

    private fun compareVersions(version1: String, version2: String): Int {
        val v1 = version1.split(".")
        val v2 = version2.split(".")

        for (i in 0 until maxOf(v1.size, v2.size)) {
            val num1 = if (i < v1.size) v1[i].toInt() else 0
            val num2 = if (i < v2.size) v2[i].toInt() else 0
            if (num1 != num2) return num1 - num2
        }
        return 0
    }

    private fun getDownloadUrl(release: GitHubRelease, abi: String): String {
        val fDroid = "fdroid"

        val assetsByAbi = release.assets.filter {
            (it.name.contains(abi, true))
        }

        val asset = if (BuildConfig.APPLICATION_ID.contains(fDroid, ignoreCase = true)) {
            assetsByAbi.firstOrNull { it.name.contains(fDroid) }
        } else {
            assetsByAbi.firstOrNull { !it.name.contains(fDroid) }
        }

        return asset?.browserDownloadUrl
            ?: throw IllegalStateException("No compatible APK found")
    }
}
