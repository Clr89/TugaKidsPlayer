package com.clr89.tugakidsplayer.ui.catalog

import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogPosterSizeTest {
    @Test
    fun largerPosterChoicesRequireWiderGridCellsAndThusReduceAdaptiveColumns() {
        assertTrue(CatalogPosterSize.SMALL.minCellWidth < CatalogPosterSize.MEDIUM.minCellWidth)
        assertTrue(CatalogPosterSize.MEDIUM.minCellWidth < CatalogPosterSize.LARGE.minCellWidth)
    }
}
