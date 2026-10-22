package com.clr89.tugakidsplayer.ui.catalog

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.clr89.tugakidsplayer.data.DemoMovieCatalog
import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.data.repository.CatalogLoadException
import com.clr89.tugakidsplayer.data.repository.RemoteMovieCatalogRepository
import com.clr89.tugakidsplayer.data.repository.PublishedRelease
import com.clr89.tugakidsplayer.data.repository.ReleaseCheckResult
import kotlinx.coroutines.CancellationException

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Content(val movies: List<Movie>, val isDemo: Boolean = false) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

@Composable
fun CatalogRoute(
    onMovieSelected: (Movie) -> Unit,
    posterSize: CatalogPosterSize,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    installedVersion: String,
    updateState: ReleaseCheckResult,
    showUpdateDialog: Boolean,
    onCheckUpdates: () -> Unit,
    onDismissUpdate: (PublishedRelease) -> Unit,
    onViewUpdate: (PublishedRelease) -> Unit,
    onOpenRepository: () -> Unit,
    onOpenWebsite: () -> Unit,
    onOpenContact: () -> Unit,
    onPosterSizeChange: (CatalogPosterSize) -> Unit,
) {
    val repository = remember { RemoteMovieCatalogRepository() }
    var retryKey by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<CatalogUiState>(CatalogUiState.Loading) }

    LaunchedEffect(retryKey) {
        state = CatalogUiState.Loading
        state = try {
            val movies = repository.loadMovies()
            CatalogUiState.Content(movies)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e("CatalogRoute", "Falha ao carregar catálogo (${error.javaClass.simpleName})", error)
            CatalogUiState.Error(
                (error as? CatalogLoadException)?.message
                    ?: "Falha inesperada ao carregar o catálogo. Consulta o registo CatalogRoute para diagnóstico.",
            )
        }
    }

    CatalogScreen(
        state = state,
        onMovieSelected = onMovieSelected,
        onRetry = { retryKey++ },
        onUseDemoCatalog = {
            state = CatalogUiState.Content(DemoMovieCatalog.movies, isDemo = true)
        },
        posterSize = posterSize,
        darkTheme = darkTheme,
        onToggleTheme = onToggleTheme,
        installedVersion = installedVersion,
        updateState = updateState,
        showUpdateDialog = showUpdateDialog,
        onCheckUpdates = onCheckUpdates,
        onDismissUpdate = onDismissUpdate,
        onViewUpdate = onViewUpdate,
        onOpenRepository = onOpenRepository,
        onOpenWebsite = onOpenWebsite,
        onOpenContact = onOpenContact,
        onPosterSizeChange = onPosterSizeChange,
    )
}
