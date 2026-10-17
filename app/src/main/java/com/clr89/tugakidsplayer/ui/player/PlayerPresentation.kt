package com.clr89.tugakidsplayer.ui.player

import androidx.media3.ui.AspectRatioFrameLayout
import java.net.URI

const val EXTERNAL_VIDEO_MIME_TYPE = "video/*"

enum class VideoPresentation(val label: String, val resizeMode: Int) {
    FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    FILL("Fill", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    STRETCH("Stretch", AspectRatioFrameLayout.RESIZE_MODE_FILL),
}

fun VideoPresentation.next(): VideoPresentation = when (this) {
    VideoPresentation.FIT -> VideoPresentation.FILL
    VideoPresentation.FILL -> VideoPresentation.STRETCH
    VideoPresentation.STRETCH -> VideoPresentation.FIT
}

/** External playback is offered only for absolute HTTPS URLs, without embedded credentials. */
fun externalPlaybackUrl(url: String): String? = runCatching {
    val uri = URI(url)
    require(uri.scheme.equals("https", ignoreCase = true))
    require(!uri.isOpaque && !uri.host.isNullOrBlank() && uri.userInfo == null)
    url
}.getOrNull()

/** Reject malformed or non-network media URLs before Media3 attempts to open them. */
fun validatePlaybackUrl(url: String): String? = runCatching {
    val uri = URI(url)
    require(!uri.isOpaque && !uri.host.isNullOrBlank() && uri.userInfo == null)
    require(uri.scheme.equals("https", true) || uri.scheme.equals("http", true))
    null
}.getOrElse { "URL inválido ou esquema não suportado" }

enum class PlaybackFailureKind { HTTP, DECODING, FORMAT_OR_CODEC, NETWORK, OTHER }

fun classifyPlaybackFailure(errorCode: Int): PlaybackFailureKind = when (errorCode) {
    androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> PlaybackFailureKind.HTTP
    androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
    androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FAILED -> PlaybackFailureKind.DECODING
    androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
    androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> PlaybackFailureKind.FORMAT_OR_CODEC
    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> PlaybackFailureKind.NETWORK
    else -> PlaybackFailureKind.OTHER
}

fun playbackFailureMessage(kind: PlaybackFailureKind): String = when (kind) {
    PlaybackFailureKind.HTTP -> "O servidor recusou o vídeo (erro HTTP). Verifica o endereço e tenta novamente."
    PlaybackFailureKind.DECODING -> "O dispositivo encontrou um erro ao descodificar o vídeo."
    PlaybackFailureKind.FORMAT_OR_CODEC -> "O formato ou codec deste vídeo pode não ser suportado por este dispositivo."
    PlaybackFailureKind.NETWORK -> "Falha de rede ao abrir o vídeo. Verifica a ligação e tenta novamente."
    PlaybackFailureKind.OTHER -> "Não foi possível reproduzir este vídeo. Consulta o diagnóstico e tenta novamente."
}
