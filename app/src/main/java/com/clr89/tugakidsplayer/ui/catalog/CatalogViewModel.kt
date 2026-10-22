package com.clr89.tugakidsplayer.ui.catalog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clr89.tugakidsplayer.data.DemoMovieCatalog
import com.clr89.tugakidsplayer.data.repository.CatalogLoadException
import com.clr89.tugakidsplayer.data.repository.MovieCatalogRepository
import com.clr89.tugakidsplayer.data.repository.RemoteMovieCatalogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val repository: MovieCatalogRepository = RemoteMovieCatalogRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _state.value = CatalogUiState.Loading
            _state.value = try {
                CatalogUiState.Content(repository.loadMovies())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(TAG, "Falha ao carregar catálogo (${error.javaClass.simpleName})", error)
                CatalogUiState.Error(
                    (error as? CatalogLoadException)?.message
                        ?: "Falha inesperada ao carregar o catálogo. Consulta o registo CatalogViewModel para diagnóstico.",
                )
            }
        }
    }

    fun useDemoCatalog() {
        _state.value = CatalogUiState.Content(DemoMovieCatalog.movies, isDemo = true)
    }

    private companion object {
        const val TAG = "CatalogViewModel"
    }
}
