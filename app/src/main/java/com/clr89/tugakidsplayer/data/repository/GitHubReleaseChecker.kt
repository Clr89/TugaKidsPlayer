package com.clr89.tugakidsplayer.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.math.BigInteger
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

data class PublishedRelease(
    val tagName: String,
    val version: String,
    val pageUrl: String,
)

sealed interface ReleaseCheckResult {
    data object NotChecked : ReleaseCheckResult
    data object Checking : ReleaseCheckResult
    data class UpdateAvailable(val release: PublishedRelease) : ReleaseCheckResult
    data object NoUpdate : ReleaseCheckResult
    data object Unavailable : ReleaseCheckResult
}

class GitHubReleaseChecker(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun check(installedVersion: String): ReleaseCheckResult = withContext(ioDispatcher) {
        val connection = try {
            URL(LATEST_RELEASE_API).openConnection() as? HttpURLConnection
                ?: return@withContext ReleaseCheckResult.Unavailable
        } catch (_: Exception) {
            return@withContext ReleaseCheckResult.Unavailable
        }

        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "TugaKidsPlayer-Android")
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NOT_FOUND -> ReleaseCheckResult.NoUpdate
                in 200..299 -> {
                    val body = connection.inputStream.use(::readBoundedResponse)
                    val release = parseLatestRelease(body) ?: return@withContext ReleaseCheckResult.Unavailable
                    val comparison = compareAppVersions(release.version, installedVersion)
                        ?: return@withContext ReleaseCheckResult.Unavailable
                    if (comparison > 0) ReleaseCheckResult.UpdateAvailable(release)
                    else ReleaseCheckResult.NoUpdate
                }
                else -> ReleaseCheckResult.Unavailable
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            ReleaseCheckResult.Unavailable
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val LATEST_RELEASE_API = "https://api.github.com/repos/Clr89/TugaKidsPlayer/releases/latest"

        fun parseLatestRelease(json: String): PublishedRelease? {
            return try {
                val tokener = JSONTokener(json)
                val value = tokener.nextValue()
                if (tokener.nextClean() != '\u0000' || value !is JSONObject) return null
                if (value.opt("draft") != false || value.opt("prerelease") != false) return null

                val tag = value.optString("tag_name").trim().takeIf { it.isNotEmpty() } ?: return null
                val version = normalizeVersion(tag) ?: return null
                val pageUrl = value.optString("html_url").trim().takeIf(::isOfficialReleaseUrl) ?: return null
                PublishedRelease(tagName = tag, version = version, pageUrl = pageUrl)
            } catch (_: JSONException) {
                null
            }
        }

        /** Returns positive/zero/negative for newer/equal/older, or null for invalid versions. */
        fun compareAppVersions(left: String, right: String): Int? {
            val leftVersion = AppVersion.parse(left) ?: return null
            val rightVersion = AppVersion.parse(right) ?: return null
            return leftVersion.compareTo(rightVersion)
        }

        fun shouldCheckAutomatically(lastCheckAt: Long?, now: Long): Boolean =
            lastCheckAt == null || now < lastCheckAt || now - lastCheckAt >= AUTO_CHECK_INTERVAL_MS

        fun shouldShowUpdate(version: String, dismissedVersion: String?, manual: Boolean): Boolean =
            manual || version != dismissedVersion

        private fun normalizeVersion(tag: String): String? {
            val version = tag.removePrefix("v").removePrefix("V")
            return version.takeIf { AppVersion.parse(it) != null }
        }

        private fun isOfficialReleaseUrl(value: String): Boolean = runCatching {
            val uri = URI(value)
            uri.scheme.equals("https", ignoreCase = true) &&
                uri.host.equals("github.com", ignoreCase = true) &&
                uri.userInfo == null &&
                uri.path.orEmpty().startsWith("/Clr89/TugaKidsPlayer/releases/tag/")
        }.getOrDefault(false)

        private fun readBoundedResponse(input: java.io.InputStream): String {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (output.size() + count > MAX_RESPONSE_BYTES) throw java.io.IOException("GitHub response too large")
                output.write(buffer, 0, count)
            }
            return output.toString(Charsets.UTF_8.name())
        }

        private const val CONNECT_TIMEOUT_MS = 5_000
        private const val READ_TIMEOUT_MS = 5_000
        private const val MAX_RESPONSE_BYTES = 512 * 1024
        private const val AUTO_CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }
}

private data class AppVersion(
    val components: List<BigInteger>,
    val prerelease: List<String>?,
) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int {
        val count = maxOf(components.size, other.components.size)
        for (index in 0 until count) {
            val left = components.getOrElse(index) { BigInteger.ZERO }
            val right = other.components.getOrElse(index) { BigInteger.ZERO }
            val result = left.compareTo(right)
            if (result != 0) return result
        }
        if (prerelease == null) return if (other.prerelease == null) 0 else 1
        if (other.prerelease == null) return -1
        val countIds = minOf(prerelease.size, other.prerelease.size)
        for (index in 0 until countIds) {
            val left = prerelease[index]
            val right = other.prerelease[index]
            val leftNumber = left.toBigIntegerOrNull()
            val rightNumber = right.toBigIntegerOrNull()
            val result = when {
                leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
                leftNumber != null -> -1
                rightNumber != null -> 1
                else -> left.compareTo(right)
            }
            if (result != 0) return result
        }
        return prerelease.size.compareTo(other.prerelease.size)
    }

    companion object {
        fun parse(input: String): AppVersion? {
            val value = input.trim().removePrefix("v").removePrefix("V")
            if (value.isEmpty() || value.count { it == '+' } > 1) return null
            val withoutBuild = value.substringBefore('+')
            if ('+' in value) {
                val build = value.substringAfter('+')
                if (build.split('.').any { !it.matches(Regex("[0-9A-Za-z-]+")) }) return null
            }
            val core = withoutBuild.substringBefore('-')
            val prereleaseText = withoutBuild.substringAfter('-', missingDelimiterValue = "")
            val components = core.split('.').map { part ->
                if (!part.matches(Regex("0|[1-9]\\d*"))) return null
                part.toBigIntegerOrNull() ?: return null
            }
            if (components.isEmpty()) return null
            val prerelease = if ('-' in withoutBuild) {
                prereleaseText.split('.').takeIf { ids ->
                    ids.isNotEmpty() && ids.all {
                        it.matches(Regex("[0-9A-Za-z-]+")) &&
                            !(it.length > 1 && it.all(Char::isDigit) && it.startsWith('0'))
                    }
                } ?: return null
            } else null
            return AppVersion(components, prerelease)
        }
    }
}
