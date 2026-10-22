package com.clr89.tugakidsplayer.ui.catalog

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.data.repository.PublishedRelease
import com.clr89.tugakidsplayer.data.repository.ReleaseCheckResult
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
    var searchState by remember { mutableStateOf(CatalogSearchState()) }
    var aboutOpen by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(searchState.isExpanded) {
        if (searchState.isExpanded) searchFocusRequester.requestFocus()
    }
    val movies = (state as? CatalogUiState.Content)?.movies.orEmpty()
    // Search stays local and applies identically to remote and explicitly selected demo movies.
    val filteredMovies = remember(searchState.query, movies) {
        movies.filter { it.title.matchesSearch(searchState.query) }
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
                Text(
                    "TugaKids",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                var sizeMenuOpen by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleTheme) {
                        ThemeToggleIcon(darkTheme)
                    }
                    if (!searchState.isExpanded) {
                        IconButton(onClick = { searchState = searchState.open() }) {
                            val iconTint = MaterialTheme.colorScheme.onSurface
                            Canvas(Modifier.size(24.dp).semantics { contentDescription = "Pesquisar" }) {
                                val radius = size.minDimension * 0.30f
                                val center = Offset(size.width * 0.40f, size.height * 0.40f)
                                drawCircle(
                                    color = iconTint,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                                drawLine(
                                    color = iconTint,
                                    start = Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
                                    end = Offset(size.width * 0.88f, size.height * 0.88f),
                                    strokeWidth = 2.dp.toPx(),
                                )
                            }
                        }
                    }
                    IconButton(onClick = { aboutOpen = true }) {
                        val iconTint = MaterialTheme.colorScheme.onSurface
                        Canvas(Modifier.size(24.dp).semantics { contentDescription = "Sobre o TugaKidsPlayer" }) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension * 0.38f
                            drawCircle(
                                color = iconTint,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                            drawCircle(
                                color = iconTint,
                                radius = 1.2.dp.toPx(),
                                center = Offset(center.x, center.y - radius * 0.38f),
                            )
                            drawLine(
                                color = iconTint,
                                start = Offset(center.x, center.y - radius * 0.05f),
                                end = Offset(center.x, center.y + radius * 0.48f),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                    }
                    Box {
                        TextButton(onClick = { sizeMenuOpen = true }) {
                            Text("Pósteres: ${posterSize.label} ▾")
                        }
                        DropdownMenu(expanded = sizeMenuOpen, onDismissRequest = { sizeMenuOpen = false }) {
                            CatalogPosterSize.entries.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text(if (posterSize == size) "✓ ${size.label}" else size.label) },
                                    onClick = { onPosterSizeChange(size); sizeMenuOpen = false },
                                )
                            }
                        }
                    }
                }
            }
            if (searchState.isExpanded) {
                OutlinedTextField(
                    value = searchState.query,
                    onValueChange = { searchState = searchState.updateQuery(it) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).focusRequester(searchFocusRequester),
                    label = { Text("Pesquisar filmes") },
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            if (searchState.query.isNotEmpty()) {
                                IconButton(onClick = { searchState = searchState.clearQuery() }) { Text("×") }
                            }
                            IconButton(onClick = {
                                searchState = searchState.close()
                                keyboardController?.hide()
                            }) { CloseSearchIcon() }
                        }
                    },
                )
            }
            when (state) {
                CatalogUiState.Loading -> Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text("A carregar o catálogo remoto…")
                }
                is CatalogUiState.Error -> Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Text("Podes tentar novamente ou abrir o catálogo de demonstração.")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onRetry) { Text("Tentar novamente") }
                        Button(onClick = onUseDemoCatalog) { Text("Usar demonstração") }
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
                                text = "A mostrar o catálogo de demonstração.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(onClick = onRetry) { Text("Tentar catálogo remoto") }
                        }
                    }
                    when {
                        movies.isEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("O catálogo remoto não contém filmes.")
                            Button(onClick = onUseDemoCatalog) { Text("Usar demonstração") }
                        }
                        filteredMovies.isEmpty() -> Text(
                            text = "Não foram encontrados filmes.",
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
    if (aboutOpen) {
        AboutDialog(
            installedVersion = installedVersion,
            updateState = updateState,
            onDismiss = { aboutOpen = false },
            onOpenRepository = onOpenRepository,
            onOpenWebsite = onOpenWebsite,
            onOpenContact = onOpenContact,
            onCheckUpdates = onCheckUpdates,
        )
    }
    val availableUpdate = (updateState as? ReleaseCheckResult.UpdateAvailable)?.release
    if (showUpdateDialog && availableUpdate != null) {
        AvailableUpdateDialog(
            installedVersion = installedVersion,
            release = availableUpdate,
            onLater = { onDismissUpdate(availableUpdate) },
            onViewRelease = { onViewUpdate(availableUpdate) },
        )
    }
}

@Composable
private fun ThemeToggleIcon(darkTheme: Boolean) {
    val iconTint = MaterialTheme.colorScheme.onSurface
    val cutoutTint = MaterialTheme.colorScheme.background
    Canvas(
        Modifier.size(24.dp).semantics {
            contentDescription = if (darkTheme) "Mudar para tema claro" else "Mudar para tema escuro"
        },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        if (darkTheme) {
            drawCircle(iconTint, radius = 7.dp.toPx(), center = center)
            repeat(8) { index ->
                val angle = Math.toRadians(index * 45.0)
                val inner = 9.dp.toPx()
                val outer = 11.dp.toPx()
                drawLine(
                    iconTint,
                    Offset(center.x + kotlin.math.cos(angle).toFloat() * inner, center.y + kotlin.math.sin(angle).toFloat() * inner),
                    Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        } else {
            val radius = 9.dp.toPx()
            drawCircle(iconTint, radius = radius, center = center)
            drawCircle(
                cutoutTint,
                radius = radius,
                center = Offset(center.x + 6.dp.toPx(), center.y - 4.dp.toPx()),
            )
        }
    }
}

@Composable
private fun CloseSearchIcon() {
    val iconTint = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(24.dp).semantics { contentDescription = "Fechar pesquisa" }) {
        val inset = 7.dp.toPx()
        drawLine(iconTint, Offset(inset, inset), Offset(size.width - inset, size.height - inset), 2.dp.toPx())
        drawLine(iconTint, Offset(size.width - inset, inset), Offset(inset, size.height - inset), 2.dp.toPx())
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
            val metadata = listOfNotNull(movie.year?.toString(), movie.ageRating).joinToString(" · ")
            if (metadata.isNotEmpty()) {
                Text(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                )
            }
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
            Text(text = "Miniatura indisponível", style = MaterialTheme.typography.bodySmall)
        }
    }
}
