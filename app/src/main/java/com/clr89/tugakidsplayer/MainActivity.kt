package com.clr89.tugakidsplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.ui.catalog.CatalogScreen
import com.clr89.tugakidsplayer.ui.player.PlayerScreen
import com.clr89.tugakidsplayer.ui.theme.TugaKidsPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TugaKidsPlayerTheme {
                var selectedMovie by remember { mutableStateOf<Movie?>(null) }
                BackHandler(enabled = selectedMovie != null) { selectedMovie = null }
                val movie = selectedMovie
                if (movie == null) {
                    CatalogScreen(onMovieSelected = { selectedMovie = it })
                } else {
                    PlayerScreen(movie = movie, onBack = { selectedMovie = null })
                }
            }
        }
    }
}
