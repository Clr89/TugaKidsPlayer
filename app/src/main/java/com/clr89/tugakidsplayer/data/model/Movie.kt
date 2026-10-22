package com.clr89.tugakidsplayer.data.model

/** A catalogue entry; [id] remains stable when display metadata changes. */
data class Movie(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val year: Int? = null,
    val ageRating: String? = null,
)
