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

    private fun assertMatches(title: String, query: String) {
        assertTrue(title.normalizeForSearch().contains(query.normalizeForSearch()))
    }
}
