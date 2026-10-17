package com.clr89.tugakidsplayer.data.repository

import com.clr89.tugakidsplayer.data.model.Movie
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

interface MovieCatalogRepository {
    suspend fun loadMovies(): List<Movie>
}

/** Downloads a small JSON catalogue on Dispatchers.IO and rejects cleartext HTTP endpoints. */
class RemoteMovieCatalogRepository(
    private val catalogUrl: String = MovieCatalogConfig.CATALOG_URL,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MovieCatalogRepository {
    override suspend fun loadMovies(): List<Movie> = withContext(ioDispatcher) {
        val connection = (URL(catalogUrl).openConnection() as? HttpsURLConnection)
            ?: throw IOException("O endereço do catálogo tem de usar HTTPS.")
        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.instanceFollowRedirects = false

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IOException("O servidor do catálogo respondeu com HTTP $responseCode.")
            }

            val response = connection.inputStream.use(::readBoundedResponse)
            try {
                MovieCatalogJsonParser.parse(response)
            } catch (error: JSONException) {
                throw IOException("A resposta do catálogo não contém JSON válido.", error)
            }
        } finally {
            connection.disconnect()
        }
    }

    // A size cap prevents a malformed or misconfigured endpoint from consuming excessive memory.
    private fun readBoundedResponse(input: java.io.InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (output.size() + count > MAX_RESPONSE_BYTES) {
                throw IOException("O ficheiro do catálogo excede o tamanho permitido.")
            }
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 12_000
        const val READ_TIMEOUT_MS = 20_000
        const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024
        const val BUFFER_SIZE = 8 * 1024
    }
}
