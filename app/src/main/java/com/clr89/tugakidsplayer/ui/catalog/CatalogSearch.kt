package com.clr89.tugakidsplayer.ui.catalog

import java.text.Normalizer
import java.util.Locale

fun String.normalizeForSearch(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
    .filterNot { character ->
        when (Character.getType(character)) {
            Character.NON_SPACING_MARK.toInt(),
            Character.COMBINING_SPACING_MARK.toInt(),
            Character.ENCLOSING_MARK.toInt() -> true
            else -> false
        }
    }
    .lowercase(Locale.ROOT)

fun String.matchesSearch(query: String): Boolean {
    val term = query.trim().normalizeForSearch()
    return term.isEmpty() || normalizeForSearch().contains(term)
}
