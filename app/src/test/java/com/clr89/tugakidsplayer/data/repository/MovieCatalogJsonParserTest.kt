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
}
