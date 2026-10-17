package com.clr89.tugakidsplayer.ui.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.clr89.tugakidsplayer.data.DemoMovieCatalog
import com.clr89.tugakidsplayer.data.repository.RemoteMovieCatalogRepository
import com.clr89.tugakidsplayer.data.model.Movie
import kotlinx.coroutines.CancellationException

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Content(val movies: List<Movie>, val isDemo: Boolean = false) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

/** Owns loading/retry state while CatalogScreen remains focused on rendering and search. */
@Composable
fun CatalogRoute(onMovieSelected: (Movie) -> Unit) {
    val repository = remember { RemoteMovieCatalogRepository() }
    var retryKey by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<CatalogUiState>(CatalogUiState.Loading) }

    LaunchedEffect(retryKey) {
        state = CatalogUiState.Loading
        state = try {
            CatalogUiState.Content(repository.loadMovies())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            CatalogUiState.Error(
                "Não foi possível carregar o catálogo remoto. Verifica a ligação e o endereço configurado.",
            )
        }
    }

    CatalogScreen(
        state = state,
        onMovieSelected = onMovieSelected,
        onRetry = { retryKey++ },
        onUseDemoCatalog = {
            // Demo data is shown only after an explicit user choice, never as a silent fallback.
            state = CatalogUiState.Content(DemoMovieCatalog.movies, isDemo = true)
        },
    )
}
