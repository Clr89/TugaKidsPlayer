package com.clr89.tugakidsplayer.ui.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.data.repository.PublishedRelease
import com.clr89.tugakidsplayer.data.repository.ReleaseCheckResult
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val catalogViewModel: CatalogViewModel = viewModel()
    val state by catalogViewModel.state.collectAsState()

    CatalogScreen(
        state = state,
        onMovieSelected = onMovieSelected,
        onRetry = catalogViewModel::reload,
        onUseDemoCatalog = catalogViewModel::useDemoCatalog,
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
