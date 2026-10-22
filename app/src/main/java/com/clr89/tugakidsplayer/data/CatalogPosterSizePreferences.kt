package com.clr89.tugakidsplayer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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

class CatalogPosterSizePreferences(context: Context) {
    private val dataStore = context.catalogPreferencesDataStore

    val posterSize: Flow<CatalogPosterSize> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[posterSizeKey].toCatalogPosterSize() }

    suspend fun savePosterSize(size: CatalogPosterSize) {
        dataStore.edit { preferences -> preferences[posterSizeKey] = size.name }
    }
}

internal fun String?.toCatalogPosterSize(): CatalogPosterSize =
    this?.let { savedValue ->
        CatalogPosterSize.entries.firstOrNull { it.name == savedValue }
    } ?: CatalogPosterSize.MEDIUM
