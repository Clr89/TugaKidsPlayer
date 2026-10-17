package com.clr89.tugakidsplayer.ui.catalog

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.clr89.tugakidsplayer.data.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onMovieSelected: (Movie) -> Unit,
    onRetry: () -> Unit,
    onUseDemoCatalog: () -> Unit,
    posterSize: CatalogPosterSize,
    onPosterSizeChange: (CatalogPosterSize) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val movies = (state as? CatalogUiState.Content)?.movies.orEmpty()
    // Search stays local and applies identically to remote and explicitly selected demo movies.
    val filteredMovies = remember(query, movies) {
        val term = query.trim()
        if (term.isEmpty()) movies else movies.filter { it.title.contains(term, ignoreCase = true) }
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Filmes", style = MaterialTheme.typography.headlineMedium)
                var sizeMenuOpen by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { sizeMenuOpen = true }) { Text("PÃ³steres: ${posterSize.label} â–¾") }
                    DropdownMenu(expanded = sizeMenuOpen, onDismissRequest = { sizeMenuOpen = false }) {
                        CatalogPosterSize.entries.forEach { size ->
                            DropdownMenuItem(
                                text = { Text(if (posterSize == size) "âœ“ ${size.label}" else size.label) },
                                onClick = { onPosterSizeChange(size); sizeMenuOpen = false },
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                label = { Text("Pesquisar filmes") },
                singleLine = true,
            )
            when (state) {
                CatalogUiState.Loading -> Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text("A carregar o catÃ¡logo remotoâ€¦")
                }
                is CatalogUiState.Error -> Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Text("Podes tentar novamente ou abrir o catÃ¡logo de demonstraÃ§Ã£o.")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onRetry) { Text("Tentar novamente") }
                        Button(onClick = onUseDemoCatalog) { Text("Usar demonstraÃ§Ã£o") }
                    }
                }
                is CatalogUiState.Content -> {
                    if (state.isDemo) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "A mostrar o catÃ¡logo de demonstraÃ§Ã£o.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(onClick = onRetry) { Text("Tentar catÃ¡logo remoto") }
                        }
                    }
                    when {
                        movies.isEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("O catÃ¡logo remoto nÃ£o contÃ©m filmes.")
                            Button(onClick = onUseDemoCatalog) { Text("Usar demonstraÃ§Ã£o") }
                        }
                        filteredMovies.isEmpty() -> Text(
                            text = "NÃ£o foram encontrados filmes.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        else -> LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = posterSize.minCellWidth),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredMovies, key = { it.id }) { movie ->
                                MovieCard(
                                    movie,
                                    onClick = { onMovieSelected(movie) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
) {
    // Focusable cards provide a visible focus target for keyboard and remote navigation.
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).focusable().semantics {
            contentDescription = "${movie.title}, filme"
        },
        shape = RoundedCornerShape(12.dp),
    ) {
        Column {
            RemoteThumbnail(movie.thumbnailUrl, movie.title)
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun RemoteThumbnail(url: String, title: String) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) {
        // Use platform networking and bitmap decoding to keep this first stage dependency-free.
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openConnection().apply { connectTimeout = 8_000; readTimeout = 8_000 }
                    .getInputStream().use { stream -> BitmapFactory.decodeStream(stream) }
            }.getOrNull()
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Miniatura de $title",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(text = "Miniatura indisponÃ­vel", style = MaterialTheme.typography.bodySmall)
        }
    }
}
