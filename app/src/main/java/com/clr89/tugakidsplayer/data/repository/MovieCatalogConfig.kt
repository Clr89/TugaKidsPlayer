package com.clr89.tugakidsplayer.data.repository

/** Set this single URL to the HTTPS endpoint that serves the movie JSON. */
object MovieCatalogConfig {
    // Reserved .invalid domain makes the unconfigured endpoint fail visibly and safely.
    const val CATALOG_URL = "https://example.invalid/tugakidsplayer/movies.json"
}
