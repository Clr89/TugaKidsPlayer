package com.clr89.tugakidsplayer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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
import com.clr89.tugakidsplayer.data.AppPreferences
import com.clr89.tugakidsplayer.data.model.Movie
import com.clr89.tugakidsplayer.data.repository.GitHubReleaseChecker
import com.clr89.tugakidsplayer.data.repository.ReleaseCheckResult
import com.clr89.tugakidsplayer.ui.catalog.CatalogRoute
import com.clr89.tugakidsplayer.ui.catalog.CatalogPosterSize
import com.clr89.tugakidsplayer.ui.player.PlayerScreen
import com.clr89.tugakidsplayer.ui.theme.TugaKidsPlayerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appPreferences = AppPreferences(applicationContext)
        val installedVersion = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "Desconhecida"
        }.getOrDefault("Desconhecida")
        setContent {
            var darkThemePreference by remember { mutableStateOf<Boolean?>(null) }
            val systemDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
            val darkTheme = darkThemePreference ?: systemDarkTheme
            TugaKidsPlayerTheme(darkTheme = darkTheme) {
                var selectedMovie by rememberSaveable(stateSaver = movieSaver) { mutableStateOf<Movie?>(null) }
                var posterSize by remember { mutableStateOf(CatalogPosterSize.MEDIUM) }
                var posterSizeWasSelected by remember { mutableStateOf(false) }
                var themeWasSelected by remember { mutableStateOf(false) }
                var dismissedReleaseVersion by remember { mutableStateOf<String?>(null) }
                var updateState by remember { mutableStateOf<ReleaseCheckResult>(ReleaseCheckResult.NotChecked) }
                var showUpdateDialog by remember { mutableStateOf(false) }
                var updateCheckJob by remember { mutableStateOf<Job?>(null) }
                val releaseChecker = remember { GitHubReleaseChecker() }
                val scope = rememberCoroutineScope()

                fun checkForUpdates(manual: Boolean, dismissedVersion: String? = dismissedReleaseVersion) {
                    updateCheckJob?.cancel()
                    updateCheckJob = scope.launch {
                        updateState = ReleaseCheckResult.Checking
                        val result = releaseChecker.check(installedVersion)
                        appPreferences.saveLastUpdateCheckAt(System.currentTimeMillis())
                        updateState = result
                        val update = (result as? ReleaseCheckResult.UpdateAvailable)?.release
                        if (update != null && GitHubReleaseChecker.shouldShowUpdate(update.version, dismissedVersion, manual)) {
                            showUpdateDialog = true
                        }
                    }
                }

                // Start with existing defaults so catalog loading never waits for disk;
                // DataStore emits saved preferences asynchronously and updates the UI.
                LaunchedEffect(appPreferences) {
                    var initialPreferencesLoaded = false
                    appPreferences.values.collect { preferences ->
                        if (!posterSizeWasSelected) posterSize = preferences.posterSize
                        if (!themeWasSelected) darkThemePreference = preferences.darkTheme
                        dismissedReleaseVersion = preferences.dismissedReleaseVersion
                        if (!initialPreferencesLoaded) {
                            initialPreferencesLoaded = true
                            if (GitHubReleaseChecker.shouldCheckAutomatically(
                                    preferences.lastUpdateCheckAt,
                                    System.currentTimeMillis(),
                                )
                            ) {
                            checkForUpdates(manual = false, dismissedVersion = preferences.dismissedReleaseVersion)
                            }
                        }
                    }
                }

                BackHandler(enabled = selectedMovie != null) { selectedMovie = null }
                val movie = selectedMovie
                if (movie == null) {
                    CatalogRoute(
                        onMovieSelected = { selectedMovie = it },
                        posterSize = posterSize,
                        darkTheme = darkTheme,
                        onToggleTheme = {
                            val updatedTheme = !darkTheme
                            themeWasSelected = true
                            darkThemePreference = updatedTheme
                            scope.launch { appPreferences.saveDarkTheme(updatedTheme) }
                        },
                        installedVersion = installedVersion,
                        updateState = updateState,
                        showUpdateDialog = showUpdateDialog,
                        onCheckUpdates = { checkForUpdates(manual = true) },
                        onDismissUpdate = { release ->
                            showUpdateDialog = false
                            scope.launch { appPreferences.saveDismissedReleaseVersion(release.version) }
                        },
                        onViewUpdate = { release ->
                            showUpdateDialog = false
                            scope.launch { appPreferences.saveDismissedReleaseVersion(release.version) }
                            openSafeExternalLink(release.pageUrl)
                        },
                        onOpenRepository = { openSafeExternalLink(REPOSITORY_URL) },
                        onOpenWebsite = { openSafeExternalLink(WEBSITE_URL) },
                        onOpenContact = { openSafeExternalLink(CONTACT_URL) },
                        onPosterSizeChange = {
                            posterSizeWasSelected = true
                            posterSize = it
                            scope.launch { appPreferences.savePosterSize(it) }
                        },
                    )
                } else {
                    PlayerScreen(movie = movie, onBack = { selectedMovie = null })
                }
            }
        }
    }

    private fun openSafeExternalLink(rawUri: String) {
        val uri = runCatching { Uri.parse(rawUri) }.getOrNull()
        if (uri == null) {
            Toast.makeText(this, "Ligação não permitida.", Toast.LENGTH_SHORT).show()
            return
        }
        val safe = when (uri.scheme?.lowercase()) {
            "https" -> uri.host?.lowercase() in setOf("github.com", "www.tugakids.com", "tugakids.com")
            "mailto" -> uri.schemeSpecificPart.equals(CONTACT_EMAIL, ignoreCase = true)
            else -> false
        }
        if (!safe) {
            Toast.makeText(this, "Ligação não permitida.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = if (uri.scheme.equals("mailto", ignoreCase = true)) {
            Intent(Intent.ACTION_SENDTO, uri)
        } else Intent(Intent.ACTION_VIEW, uri)
        try {
            if (intent.resolveActivity(packageManager) == null) {
                Toast.makeText(this, "Não há uma aplicação compatível para abrir esta ligação.", Toast.LENGTH_LONG).show()
                return
            }
            startActivity(Intent.createChooser(intent, if (uri.scheme == "mailto") "Enviar email" else "Abrir ligação"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Não há uma aplicação compatível para abrir esta ligação.", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível abrir esta ligação.", Toast.LENGTH_LONG).show()
        }
    }
}

private const val REPOSITORY_URL = "https://github.com/Clr89/TugaKidsPlayer"
private const val WEBSITE_URL = "https://www.tugakids.com"
private const val CONTACT_EMAIL = "tugakids.com@proton.me"
private const val CONTACT_URL = "mailto:$CONTACT_EMAIL"

private val movieSaver = listSaver<Movie?, String>(
    save = { movie -> movie?.let { listOf(it.id, it.title, it.thumbnailUrl, it.videoUrl) } ?: emptyList() },
    restore = { values -> values.takeIf { it.size == 4 }?.let { Movie(it[0], it[1], it[2], it[3]) } },
)
