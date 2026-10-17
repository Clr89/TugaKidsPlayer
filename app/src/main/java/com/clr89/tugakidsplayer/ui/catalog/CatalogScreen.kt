package com.clr89.tugakidsplayer.ui.catalog

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.clr89.tugakidsplayer.data.DemoMovieCatalog
import com.clr89.tugakidsplayer.data.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun CatalogScreen(onMovieSelected: (Movie) -> Unit) {
    var query by remember { mutableStateOf("") }
    // Filtering is local and immediate, so typing and remote keyboard input behave alike.
    val filteredMovies = remember(query) {
        val term = query.trim()
        if (term.isEmpty()) DemoMovieCatalog.movies
        else DemoMovieCatalog.movies.filter { it.title.contains(term, ignoreCase = true) }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 20.dp),
        ) {
            Text(
                text = "Filmes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                label = { Text("Pesquisar filmes") },
                singleLine = true,
            )
            if (filteredMovies.isEmpty()) {
                Text(
                    text = "Não foram encontrados filmes.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredMovies, key = { it.id }) { movie ->
                        MovieCard(movie, onClick = { onMovieSelected(movie) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCard(movie: Movie, onClick: () -> Unit) {
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
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Miniatura de $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(text = "Miniatura indisponível", style = MaterialTheme.typography.bodySmall)
        }
    }
}
