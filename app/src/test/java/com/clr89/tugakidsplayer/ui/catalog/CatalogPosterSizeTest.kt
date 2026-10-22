package com.clr89.tugakidsplayer.ui.catalog

import com.clr89.tugakidsplayer.data.toCatalogPosterSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogPosterSizeTest {
    @Test
    fun largerPosterChoicesRequireWiderGridCellsAndThusReduceAdaptiveColumns() {
        assertTrue(CatalogPosterSize.SMALL.minCellWidth < CatalogPosterSize.MEDIUM.minCellWidth)
        assertTrue(CatalogPosterSize.MEDIUM.minCellWidth < CatalogPosterSize.LARGE.minCellWidth)
    }

    @Test
    fun savedNamesMapToTheirPosterSizes() {
        assertEquals(CatalogPosterSize.SMALL, "SMALL".toCatalogPosterSize())
        assertEquals(CatalogPosterSize.MEDIUM, "MEDIUM".toCatalogPosterSize())
        assertEquals(CatalogPosterSize.LARGE, "LARGE".toCatalogPosterSize())
    }

    @Test
    fun unknownOrMissingSavedValuesFallBackToMedium() {
        assertEquals(CatalogPosterSize.MEDIUM, "EXTRA_LARGE".toCatalogPosterSize())
        assertEquals(CatalogPosterSize.MEDIUM, "".toCatalogPosterSize())
        assertEquals(CatalogPosterSize.MEDIUM, null.toCatalogPosterSize())
    }
}
