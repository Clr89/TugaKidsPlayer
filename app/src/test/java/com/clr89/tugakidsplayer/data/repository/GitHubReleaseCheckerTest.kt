package com.clr89.tugakidsplayer.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseCheckerTest {
    @Test
    fun comparesEqualNewerAndOlderVersionsIncludingMultipleDigits() {
        assertEquals(0, GitHubReleaseChecker.compareAppVersions("1.0", "v1.0.0"))
        assertTrue(GitHubReleaseChecker.compareAppVersions("1.10.0", "1.9.9")!! > 0)
        assertTrue(GitHubReleaseChecker.compareAppVersions("2.0", "10.0")!! < 0)
        assertTrue(GitHubReleaseChecker.compareAppVersions("v1.2.0", "1.2.1")!! < 0)
    }

    @Test
    fun comparesPrereleaseBeforeStableAndIgnoresBuildMetadata() {
        assertTrue(GitHubReleaseChecker.compareAppVersions("1.0.0", "1.0.0-rc.2")!! > 0)
        assertTrue(GitHubReleaseChecker.compareAppVersions("1.0.0-rc.10", "1.0.0-rc.2")!! > 0)
        assertEquals(0, GitHubReleaseChecker.compareAppVersions("1.0+build.2", "v1.0+build.9"))
        assertNull(GitHubReleaseChecker.compareAppVersions("versão", "1.0"))
        assertNull(GitHubReleaseChecker.compareAppVersions("1.02", "1.2"))
        assertNull(GitHubReleaseChecker.compareAppVersions("1.0+", "1.0"))
    }

    @Test
    fun automaticCheckRunsOncePerDayAndManualChecksCanBypassCooldown() {
        assertTrue(GitHubReleaseChecker.shouldCheckAutomatically(null, 10_000L))
        assertTrue(GitHubReleaseChecker.shouldCheckAutomatically(1_000L, 1_000L + 86_400_000L))
        assertTrue(GitHubReleaseChecker.shouldCheckAutomatically(2_000L, 1_000L))
        assertEquals(false, GitHubReleaseChecker.shouldCheckAutomatically(1_000L, 1_000L + 10_000L))
    }

    @Test
    fun dismissedVersionIsSuppressedAutomaticallyButCanBeShownManually() {
        assertEquals(false, GitHubReleaseChecker.shouldShowUpdate("1.2.0", "1.2.0", manual = false))
        assertEquals(true, GitHubReleaseChecker.shouldShowUpdate("1.3.0", "1.2.0", manual = false))
        assertEquals(true, GitHubReleaseChecker.shouldShowUpdate("1.2.0", "1.2.0", manual = true))
    }

    @Test
    fun parsesOnlyPublishedReleaseWithValidVersionAndOfficialReleasePage() {
        val release = GitHubReleaseChecker.parseLatestRelease(
            """{"tag_name":"v1.12.0","html_url":"https://github.com/Clr89/TugaKidsPlayer/releases/tag/v1.12.0","draft":false,"prerelease":false}""",
        )
        assertEquals("v1.12.0", release?.tagName)
        assertEquals("1.12.0", release?.version)
        assertEquals("https://github.com/Clr89/TugaKidsPlayer/releases/tag/v1.12.0", release?.pageUrl)
    }

    @Test
    fun rejectsDraftPrereleaseMalformedResponseAndUntrustedReleaseUrl() {
        val base = "\"tag_name\":\"v1.1.0\",\"html_url\":\"https://github.com/Clr89/TugaKidsPlayer/releases/tag/v1.1.0\""
        assertNull(GitHubReleaseChecker.parseLatestRelease("""{$base,"draft":true,"prerelease":false}"""))
        assertNull(GitHubReleaseChecker.parseLatestRelease("""{$base,"draft":false,"prerelease":true}"""))
        assertNull(
            GitHubReleaseChecker.parseLatestRelease(
                """{"tag_name":"latest","html_url":"https://github.com/Clr89/TugaKidsPlayer/releases/tag/latest","draft":false,"prerelease":false}""",
            ),
        )
        assertNull(
            GitHubReleaseChecker.parseLatestRelease(
                """{"tag_name":"v1.1.0","html_url":"https://example.org/releases/tag/v1.1.0","draft":false,"prerelease":false}""",
            ),
        )
        assertNull(GitHubReleaseChecker.parseLatestRelease("not json"))
        assertNull(GitHubReleaseChecker.parseLatestRelease("""{"message":"Not Found"}"""))
        assertNull(
            GitHubReleaseChecker.parseLatestRelease(
                """{"tag_name":"v1.1.0","html_url":"https://github.com/Clr89/TugaKidsPlayer/releases/tag/v1.1.0","draft":"false","prerelease":false}""",
            ),
        )
    }
}
