package com.clr89.tugakidsplayer.ui.catalog

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CatalogPosterSize(val label: String, val minCellWidth: Dp) {
    SMALL("Pequenos", 120.dp),
    MEDIUM("Médios", 172.dp),
    LARGE("Grandes", 236.dp),
}
