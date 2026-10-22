package com.clr89.tugakidsplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import com.clr89.tugakidsplayer.data.CatalogPosterSizePreferences
import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.ui.catalog.CatalogRoute
import com.clr89.tugakidsplayer.ui.catalog.CatalogPosterSize
import com.clr89.tugakidsplayer.ui.player.PlayerScreen
import com.clr89.tugakidsplayer.ui.theme.TugaKidsPlayerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val posterSizePreferences = CatalogPosterSizePreferences(applicationContext)
        setContent {
            TugaKidsPlayerTheme {
                var selectedMovie by rememberSaveable(stateSaver = movieSaver) { mutableStateOf<Movie?>(null) }
                var posterSize by remember { mutableStateOf(CatalogPosterSize.MEDIUM) }
                var posterSizeWasSelected by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                // Start with MEDIUM so catalog loading never waits for disk; DataStore
                // emits the saved choice asynchronously and updates the catalog then.
                LaunchedEffect(posterSizePreferences) {
                    posterSizePreferences.posterSize.collect {
                        if (!posterSizeWasSelected) posterSize = it
                    }
                }

                BackHandler(enabled = selectedMovie != null) { selectedMovie = null }
                val movie = selectedMovie
                if (movie == null) {
                    CatalogRoute(
                        onMovieSelected = { selectedMovie = it },
                        posterSize = posterSize,
                        onPosterSizeChange = {
                            posterSizeWasSelected = true
                            posterSize = it
                            scope.launch { posterSizePreferences.savePosterSize(it) }
                        },
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
