package com.clr89.tugakidsplayer.ui.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogSearchTest {
    @Test
    fun matchesPortugueseWordsWithOrWithoutAccents() {
        assertMatches("ação", "acao")
        assertMatches("pão", "pao")
        assertMatches("José", "jose")
    }

    @Test
    fun ignoresCaseAndDiacriticDifferences() {
        assertMatches("AÇÃO", "ação")
        assertMatches("José", "JOSÉ")
        assertMatches("jose", "José")
    }

    @Test
    fun emptyAndNonMatchingSearchTermsBehaveAsExpected() {
        assertTrue("ação".matchesSearch(""))
        assertTrue("ação".matchesSearch("   "))
        assertFalse("aventura".matchesSearch("ficção"))
    }

    @Test
    fun removesCombiningDiacriticalMarksWithoutChangingSourceText() {
        val original = "ação"
        val query = "acao"

        assertTrue(original.normalizeForSearch().contains(query.normalizeForSearch()))
        assertEquals("ação", original)
    }

    @Test
    fun expandableSearchOpensKeepsQueryOnClearAndClosesWithFullCatalogueQuery() {
        val opened = CatalogSearchState().open()
        assertTrue(opened.isExpanded)
        assertEquals("aventura", opened.updateQuery("aventura").query)

        val cleared = opened.updateQuery("aventura").clearQuery()
        assertTrue(cleared.isExpanded)
        assertEquals("", cleared.query)

        val closed = opened.updateQuery("aventura").close()
        assertFalse(closed.isExpanded)
        assertEquals("", closed.query)
        assertTrue("Qualquer filme".matchesSearch(closed.query))
    }

    private fun assertMatches(title: String, query: String) {
        assertTrue(title.normalizeForSearch().contains(query.normalizeForSearch()))
    }
}
