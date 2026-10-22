package com.clr89.tugakidsplayer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.clr89.tugakidsplayer.ui.catalog.CatalogPosterSize
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// A top-level delegate ensures the app uses one DataStore instance for this file.
private val Context.catalogPreferencesDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "catalog_preferences")

private val posterSizeKey = stringPreferencesKey("catalog_poster_size")
private val themeKey = stringPreferencesKey("app_theme")
private val dismissedReleaseVersionKey = stringPreferencesKey("dismissed_update_version")
private val lastUpdateCheckKey = longPreferencesKey("last_update_check_at")

class AppPreferences(context: Context) {
    private val dataStore = context.catalogPreferencesDataStore

    val values: Flow<AppPreferenceValues> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppPreferenceValues(
                posterSize = preferences[posterSizeKey].toCatalogPosterSize(),
                darkTheme = preferences[themeKey].toDarkThemePreference(),
                dismissedReleaseVersion = preferences[dismissedReleaseVersionKey],
                lastUpdateCheckAt = preferences[lastUpdateCheckKey],
            )
        }

    suspend fun savePosterSize(size: CatalogPosterSize) {
        dataStore.edit { preferences -> preferences[posterSizeKey] = size.name }
    }

    suspend fun saveDarkTheme(darkTheme: Boolean) {
        dataStore.edit { preferences -> preferences[themeKey] = darkTheme.toThemePreferenceValue() }
    }

    suspend fun saveDismissedReleaseVersion(version: String) {
        dataStore.edit { preferences -> preferences[dismissedReleaseVersionKey] = version }
    }

    suspend fun saveLastUpdateCheckAt(timestamp: Long) {
        dataStore.edit { preferences -> preferences[lastUpdateCheckKey] = timestamp }
    }
}

data class AppPreferenceValues(
    val posterSize: CatalogPosterSize,
    /** Null means no explicit choice has been saved; the app may follow the system initially. */
    val darkTheme: Boolean?,
    val dismissedReleaseVersion: String?,
    val lastUpdateCheckAt: Long?,
)

internal fun String?.toCatalogPosterSize(): CatalogPosterSize =
    this?.let { savedValue ->
        CatalogPosterSize.entries.firstOrNull { it.name == savedValue }
    } ?: CatalogPosterSize.MEDIUM

internal fun String?.toDarkThemePreference(): Boolean? = when (this) {
    "dark" -> true
    "light" -> false
    else -> null
}

internal fun Boolean.toThemePreferenceValue(): String = if (this) "dark" else "light"
