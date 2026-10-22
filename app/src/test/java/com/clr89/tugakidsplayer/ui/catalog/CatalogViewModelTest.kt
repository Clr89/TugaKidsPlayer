package com.clr89.tugakidsplayer.ui.catalog

import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.data.repository.MovieCatalogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsRemoteCatalogAndSupportsDemoCatalog() = runTest(dispatcher) {
        val movie = Movie("id", "Filme", "https://example.org/poster.jpg", "https://example.org/video.mp4")
        val viewModel = CatalogViewModel(FakeCatalogRepository(listOf(movie)))

        advanceUntilIdle()

        assertEquals(CatalogUiState.Content(listOf(movie)), viewModel.state.value)

        viewModel.useDemoCatalog()
        assertEquals(true, (viewModel.state.value as CatalogUiState.Content).isDemo)
    }

    private class FakeCatalogRepository(
        private val movies: List<Movie>,
    ) : MovieCatalogRepository {
        override suspend fun loadMovies(): List<Movie> = movies
    }
}
