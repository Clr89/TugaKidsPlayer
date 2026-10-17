package com.clr89.tugakidsplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.ui.catalog.CatalogRoute
import com.clr89.tugakidsplayer.ui.catalog.CatalogPosterSize
import com.clr89.tugakidsplayer.ui.player.PlayerScreen
import com.clr89.tugakidsplayer.ui.theme.TugaKidsPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TugaKidsPlayerTheme {
                var selectedMovie by rememberSaveable(stateSaver = movieSaver) { mutableStateOf<Movie?>(null) }
                var posterSize by rememberSaveable { mutableStateOf(CatalogPosterSize.MEDIUM) }
                BackHandler(enabled = selectedMovie != null) { selectedMovie = null }
                val movie = selectedMovie
                if (movie == null) {
                    CatalogRoute(
                        onMovieSelected = { selectedMovie = it },
                        posterSize = posterSize,
                        onPosterSizeChange = { posterSize = it },
                    )
                } else {
                    PlayerScreen(movie = movie, onBack = { selectedMovie = null })
                }
            }
        }
    }
}

private val movieSaver = listSaver<Movie?, String>(
    save = { movie -> movie?.let { listOf(it.id, it.title, it.thumbnailUrl, it.videoUrl) } ?: emptyList() },
    restore = { values -> values.takeIf { it.size == 4 }?.let { Movie(it[0], it[1], it[2], it[3]) } },
)
