package com.clr89.tugakidsplayer.data.repository

import com.clr89.tugakidsplayer.data.model.Movie
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URI

/** Converts either a JSON array or an object containing a `movies` array into valid movies. */
object MovieCatalogJsonParser {
    fun parse(json: String): List<Movie> {
        val tokener = JSONTokener(json)
        val root = tokener.nextValue()
        if (tokener.nextClean() != '\u0000') {
            throw JSONException("Unexpected content after the catalogue JSON")
        }
        val entries = when (root) {
            is JSONArray -> root
            is JSONObject -> root.opt("movies") as? JSONArray
                ?: throw JSONException("Expected a 'movies' array in the catalogue response")
            else -> throw JSONException("Expected a JSON array or an object containing 'movies'")
        }

        val movies = mutableListOf<Movie>()
        val seenIds = mutableSetOf<String>()
        for (index in 0 until entries.length()) {
            val entry = entries.optJSONObject(index) ?: continue
            val movie = entry.toMovieOrNull() ?: continue
            // Use the playback URL as a stable ID because the remote schema has no ID field.
            if (seenIds.add(movie.id)) movies += movie
        }
        return movies
    }

    private fun JSONObject.toMovieOrNull(): Movie? {
        val title = (opt("title") as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val thumbnailUrl = (opt("thumbnail") as? String)?.trim()?.takeIf { it.isHttpsUrl() } ?: return null
        val videoUrl = (opt("externallink") as? String)?.trim()?.takeIf { it.isHttpsUrl() } ?: return null

        return Movie(
            id = videoUrl,
            title = title,
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
        )
    }

    private fun String?.isHttpsUrl(): Boolean {
        if (this.isNullOrBlank()) return false
        return runCatching {
            val uri = URI(this.trim())
            uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank() && uri.userInfo == null
        }.getOrDefault(false)
    }
}
