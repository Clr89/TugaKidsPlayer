package com.clr89.tugakidsplayer.data.repository

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieCatalogJsonParserTest {
    @Test
    fun parsesRequiredFieldsFromArrayAndMoviesObject() {
        val arrayJson = """
            [{"title":"Filme de teste","thumbnail":"https://example.org/poster.jpg","externallink":"https://example.org/video.mp4"}]
        """.trimIndent()
        val json = """
            {"movies":[{"title":"Filme de teste","thumbnail":"https://example.org/poster.jpg","externallink":"https://example.org/video.mp4"}]}
        """.trimIndent()

        val fromArray = MovieCatalogJsonParser.parse(arrayJson).single()
        val movie = MovieCatalogJsonParser.parse(json).single()

        assertEquals(movie, fromArray)
        assertEquals("Filme de teste", movie.title)
        assertEquals("https://example.org/poster.jpg", movie.thumbnailUrl)
        assertEquals("https://example.org/video.mp4", movie.videoUrl)
        assertEquals(movie.videoUrl, movie.id)
    }

    @Test
    fun ignoresEntriesWithMissingOrInvalidRequiredFields() {
        val json = """
            [
              {"title":"Sem miniatura","externallink":"https://example.org/a.mp4"},
              {"title":"   ","thumbnail":"https://example.org/b.jpg","externallink":"https://example.org/b.mp4"},
              {"title":"Sem vÃ­deo","thumbnail":"https://example.org/c.jpg"},
              {"title":"HTTP nÃ£o permitido","thumbnail":"http://example.org/d.jpg","externallink":"https://example.org/d.mp4"},
              {"title":"VÃ¡lido","thumbnail":"https://example.org/e.jpg","externallink":"https://example.org/e.mp4"}
            ]
        """.trimIndent()

        val movies = MovieCatalogJsonParser.parse(json)

        assertEquals(1, movies.size)
        assertEquals("VÃ¡lido", movies.single().title)
    }

    @Test
    fun normalizesHttpUrlsForAuthorizedHostsAndPreservesTheirComponents() {
        val movie = parseMovie(
            thumbnail = "http://capa.tugakids.com/capas/32890033.jpg?size=large&lang=pt#cover",
            video = "http://tkapp26.buzz/32890033.mp4?token=abc#play",
        )

        assertEquals("https://capa.tugakids.com/capas/32890033.jpg?size=large&lang=pt#cover", movie.thumbnailUrl)
        assertEquals("https://tkapp26.buzz/32890033.mp4?token=abc#play", movie.videoUrl)
    }

    @Test
    fun preservesValidHttpsUrls() {
        val movie = parseMovie(
            thumbnail = "https://capa.tugakids.com/capas/32890033.jpg?size=large",
            video = "https://tkapp26.buzz/32890033.mp4?token=abc",
        )

        assertEquals("https://capa.tugakids.com/capas/32890033.jpg?size=large", movie.thumbnailUrl)
        assertEquals("https://tkapp26.buzz/32890033.mp4?token=abc", movie.videoUrl)
    }

    @Test
    fun rejectsHttpUrlsOutsideAuthorizedHosts() {
        assertCatalogEntryRejected(
            thumbnail = "http://other.example/capas/32890033.jpg",
            video = "https://tkapp26.buzz/32890033.mp4",
        )
        assertCatalogEntryRejected(
            thumbnail = "https://capa.tugakids.com/capas/32890033.jpg",
            video = "http://sub.tkapp26.buzz/32890033.mp4",
        )
    }

    @Test
    fun rejectsMalformedUrlsAndUnsupportedSchemes() {
        listOf("https://bad host/image.jpg", "https://example.org:bad/image.jpg", "https://", "not a URL").forEach { url ->
            assertCatalogEntryRejected(thumbnail = url, video = "https://tkapp26.buzz/32890033.mp4")
        }
        listOf("ftp://tkapp26.buzz/32890033.mp4", "file:///video.mp4", "javascript:alert(1)").forEach { url ->
            assertCatalogEntryRejected(
                thumbnail = "https://capa.tugakids.com/capas/32890033.jpg",
                video = url,
            )
        }
    }

    @Test
    fun parsesEmptyCatalogues() {
        assertTrue(MovieCatalogJsonParser.parse("[]").isEmpty())
        assertTrue(MovieCatalogJsonParser.parse("{\"movies\":[]}").isEmpty())
    }

    @Test
    fun rejectsMalformedOrUnexpectedJsonResponses() {
        listOf("{invalid", "null", "{}", "[] trailing").forEach { response ->
            try {
                MovieCatalogJsonParser.parse(response)
                throw AssertionError("Expected invalid JSON response to be rejected: $response")
            } catch (_: JSONException) {
                // Expected: malformed syntax, an unsupported root, or trailing response content.
            }
        }
    }

    private fun parseMovie(thumbnail: String, video: String) = MovieCatalogJsonParser.parse(
        """[{"title":"Filme","thumbnail":"$thumbnail","externallink":"$video"}]""",
    ).single()

    private fun assertCatalogEntryRejected(thumbnail: String, video: String) {
        val json = """[{"title":"Filme","thumbnail":"$thumbnail","externallink":"$video"}]"""
        assertTrue(MovieCatalogJsonParser.parse(json).isEmpty())
    }
}
