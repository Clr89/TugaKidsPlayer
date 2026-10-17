package com.clr89.tugakidsplayer.data.repository

import android.util.Log
import com.clr89.tugakidsplayer.data.model.Movie
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

interface MovieCatalogRepository { suspend fun loadMovies(): List<Movie> }

enum class CatalogFailure { NETWORK, HTTP, PARSING, CONFIGURATION }

class CatalogLoadException(val failure: CatalogFailure, message: String, cause: Throwable? = null) : IOException(message, cause)

/** Downloads a small JSON catalogue on IO; the response media type is advisory because this endpoint serves octet-stream. */
class RemoteMovieCatalogRepository(
    private val catalogUrl: String = MovieCatalogConfig.CATALOG_URL,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MovieCatalogRepository {
    override suspend fun loadMovies(): List<Movie> = withContext(ioDispatcher) {
        val safeEndpoint = runCatching { URL(catalogUrl).let { "${it.host}${it.path}" } }.getOrDefault("endpoint invÃ¡lido")
        val connection = try {
            URL(catalogUrl).openConnection() as? HttpsURLConnection
                ?: throw CatalogLoadException(CatalogFailure.CONFIGURATION, "O endereÃ§o do catÃ¡logo tem de usar HTTPS.")
        } catch (error: CatalogLoadException) { throw error }
        catch (error: Exception) {
            Log.e(TAG, "URL do catÃ¡logo invÃ¡lido ($safeEndpoint)", error)
            throw CatalogLoadException(CatalogFailure.CONFIGURATION, "O endereÃ§o do catÃ¡logo Ã© invÃ¡lido.", error)
        }
        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.instanceFollowRedirects = false

            val responseCode = try { connection.responseCode } catch (error: Exception) {
                Log.e(TAG, "Falha de rede/TLS ao contactar $safeEndpoint (${error.javaClass.simpleName})", error)
                throw CatalogLoadException(CatalogFailure.NETWORK, "Falha de rede ou TLS ao contactar o catÃ¡logo. Verifica a ligaÃ§Ã£o, a data/hora e o certificado do servidor.", error)
            }
            val contentType = connection.contentType?.substringBefore(';') ?: "ausente"
            Log.i(TAG, "Resposta do catÃ¡logo: endpoint=$safeEndpoint http=$responseCode contentType=$contentType")
            if (responseCode !in 200..299) {
                throw CatalogLoadException(CatalogFailure.HTTP, "O servidor do catÃ¡logo respondeu com HTTP $responseCode.")
            }

            val response = try { connection.inputStream.use(::readBoundedResponse) }
            catch (error: CatalogLoadException) { throw error }
            catch (error: Exception) {
                Log.e(TAG, "Falha ao ler corpo do catÃ¡logo ($safeEndpoint, ${error.javaClass.simpleName})", error)
                throw CatalogLoadException(CatalogFailure.NETWORK, "A ligaÃ§Ã£o foi iniciada, mas nÃ£o foi possÃ­vel ler a resposta do catÃ¡logo.", error)
            }
            try {
                MovieCatalogJsonParser.parse(response)
            } catch (error: JSONException) {
                Log.e(TAG, "JSON invÃ¡lido recebido de $safeEndpoint (contentType=$contentType, bytes=${response.toByteArray(Charsets.UTF_8).size})", error)
                throw CatalogLoadException(CatalogFailure.PARSING, "O servidor respondeu, mas o conteÃºdo recebido nÃ£o Ã© um catÃ¡logo JSON vÃ¡lido.", error)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun readBoundedResponse(input: java.io.InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (output.size() + count > MAX_RESPONSE_BYTES) throw IOException("O ficheiro do catÃ¡logo excede o tamanho permitido.")
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }

    private companion object {
        const val TAG = "RemoteMovieCatalog"
        const val CONNECT_TIMEOUT_MS = 12_000
        const val READ_TIMEOUT_MS = 20_000
        const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024
        const val BUFFER_SIZE = 8 * 1024
    }
}
