package com.clr89.tugakidsplayer.ui.player

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import com.clr89.tugakidsplayer.data.model.Movie
import java.net.URI

@Composable
fun PlayerScreen(movie: Movie, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val activity = context as? Activity
    var player by remember(movie.id) { mutableStateOf<ExoPlayer?>(null) }
    var playerView by remember(movie.id) { mutableStateOf<PlayerView?>(null) }
    var playbackError by remember(movie.id) { mutableStateOf<String?>(null) }
    var externalOpenMessage by remember(movie.id) { mutableStateOf<String?>(null) }
    var savedPositionMs by rememberSaveable(movie.id) { mutableStateOf(0L) }
    var savedPlayWhenReady by rememberSaveable(movie.id) { mutableStateOf(true) }
    var presentation by rememberSaveable(movie.id) { mutableStateOf(VideoPresentation.FIT) }
    var controlsVisible by remember(movie.id) { mutableStateOf(true) }
    var externalLaunchPending by remember(movie.id) { mutableStateOf(false) }
    var menuExpanded by remember(movie.id) { mutableStateOf(false) }
    var presentationMenuExpanded by remember(movie.id) { mutableStateOf(false) }

    LaunchedEffect(controlsVisible) {
        if (!controlsVisible) {
            menuExpanded = false
            presentationMenuExpanded = false
        }
    }

    // Immersive mode belongs to the player destination and restores the bars that were visible on entry.
    DisposableEffect(activity, movie.id) {
        val window = activity?.window
        val decor = window?.decorView
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        val insets = decor?.let(ViewCompat::getRootWindowInsets)
        val statusWasVisible = insets?.isVisible(WindowInsetsCompat.Type.statusBars()) ?: true
        val navigationWasVisible = insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) ?: true
        val oldBehavior = controller?.systemBarsBehavior
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            if (statusWasVisible) controller?.show(WindowInsetsCompat.Type.statusBars())
            else controller?.hide(WindowInsetsCompat.Type.statusBars())
            if (navigationWasVisible) controller?.show(WindowInsetsCompat.Type.navigationBars())
            else controller?.hide(WindowInsetsCompat.Type.navigationBars())
            if (oldBehavior != null) controller?.systemBarsBehavior = oldBehavior
        }
    }

    DisposableEffect(lifecycleOwner, movie.id, movie.videoUrl) {
        fun createPlayer() {
            if (player != null) return
            playbackError = validatePlaybackUrl(movie.videoUrl)
            if (playbackError != null) {
                Log.e(TAG, "URL inválido para filme '${movie.title}'; motivo=$playbackError")
                return
            }
            playbackError = null
            player = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        val kind = classifyPlaybackFailure(error.errorCode)
                        val httpCode = generateSequence(error as Throwable?) { it.cause }
                            .filterIsInstance<HttpDataSource.InvalidResponseCodeException>()
                            .firstOrNull()?.responseCode
                        val safeUrl = runCatching { URI(movie.videoUrl).let { "${it.host}${it.path}" } }
                            .getOrDefault("URL inválido")
                        val causeTypes = generateSequence(error as Throwable?) { it.cause }
                            .joinToString(" -> ") { it.javaClass.simpleName }
                        Log.e(TAG, "Playback falhou: kind=$kind code=${error.errorCode} http=$httpCode url=$safeUrl causes=$causeTypes message=${redactUrlQuery(error.message.orEmpty())}")
                        playbackError = if (httpCode != null) {
                            "O servidor respondeu com HTTP $httpCode ao abrir o vídeo."
                        } else playbackFailureMessage(kind)
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        Log.i(TAG, "Dimensão do vídeo: ${videoSize.width}x${videoSize.height}, pixelRatio=${videoSize.pixelWidthHeightRatio}, rotation=${videoSize.unappliedRotationDegrees}")
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        val descriptions = tracks.groups.flatMap { group ->
                            (0 until group.length).map { index ->
                                val format = group.getTrackFormat(index)
                                "${format.sampleMimeType ?: "?"}/${format.codecs ?: "?"}/${format.width}x${format.height}, selecionada=${group.isTrackSelected(index)}"
                            }
                        }
                        Log.i(TAG, "Tracks de mídia: ${descriptions.joinToString()}")
                    }
                })
                addAnalyticsListener(object : AnalyticsListener {
                    override fun onVideoDecoderInitialized(
                        eventTime: AnalyticsListener.EventTime,
                        decoderName: String,
                        initializedTimestampMs: Long,
                        initializationDurationMs: Long,
                    ) {
                        Log.i(TAG, "Descodificador de vídeo: $decoderName (inicialização ${initializationDurationMs}ms)")
                    }
                })
                setMediaItem(MediaItem.fromUri(movie.videoUrl), savedPositionMs)
                prepare()
                playWhenReady = savedPlayWhenReady
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> createPlayer()
                Lifecycle.Event.ON_PAUSE -> {
                    if (!externalLaunchPending) {
                        savedPositionMs = player?.currentPosition ?: savedPositionMs
                        savedPlayWhenReady = player?.playWhenReady ?: savedPlayWhenReady
                    }
                    player?.pause()
                }
                // Release the decoder at STOP; saved position and play intent restore across Activity recreation.
                Lifecycle.Event.ON_STOP -> {
                    player?.release()
                    player = null
                    playerView = null
                }
                Lifecycle.Event.ON_RESUME -> {
                    player?.let { activePlayer ->
                        if (activePlayer.playWhenReady != savedPlayWhenReady) {
                            activePlayer.playWhenReady = savedPlayWhenReady
                        }
                    }
                    externalLaunchPending = false
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) createPlayer()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player?.release()
            player = null
            playerView = null
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        val activePlayer = player
        if (activePlayer != null) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        resizeMode = presentation.resizeMode
                        useController = true
                        player = activePlayer
                        controllerShowTimeoutMs = DEFAULT_CONTROLLER_TIMEOUT_MS
                        // The visibility listener reports the native view only after its fade ends.
                        // Disable that animation so Compose controls change with Media3 immediately.
                        setControllerAnimationEnabled(false)
                        setControllerVisibilityListener(ControllerVisibilityListener { visibility ->
                            controlsVisible = visibility == View.VISIBLE
                        })
                        playerView = this

                        showController()
                    }
                },
                update = { view ->
                    view.player = activePlayer
                    view.resizeMode = presentation.resizeMode
                    playerView = view
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else if (playbackError == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        if (controlsVisible) {
            Box(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(104.dp)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent))),
            )
            Row(
                modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(52.dp).semantics {
                    contentDescription = "Voltar ao catálogo"
                }) { Text("←", color = Color.White, fontSize = 28.sp) }
                Spacer(Modifier.width(12.dp))
                Text(
                    movie.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(52.dp).semantics {
                            contentDescription = "Mais opções do leitor"
                        },
                    ) {
                        Text("⋮", color = Color.White, fontSize = 28.sp)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ajustar imagem") },
                            trailingIcon = { Text("›") },
                            onClick = {
                                menuExpanded = false
                                presentationMenuExpanded = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Abrir com") },
                            onClick = {
                                menuExpanded = false
                                externalOpenMessage = null
                                val externalUrl = externalPlaybackUrl(movie.videoUrl)
                                val currentActivity = activity
                                if (externalUrl == null) {
                                    externalOpenMessage = "Só é possível abrir externamente um URL HTTPS válido."
                                } else if (currentActivity == null) {
                                    externalOpenMessage = "Não foi possível abrir outra aplicação neste ecrã."
                                } else {
                                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(externalUrl), EXTERNAL_VIDEO_MIME_TYPE)
                                    }
                                    val readers = currentActivity.packageManager.queryIntentActivities(
                                        viewIntent,
                                        PackageManager.MATCH_DEFAULT_ONLY,
                                    )
                                    if (readers.isEmpty()) {
                                        externalOpenMessage = "Não há aplicações compatíveis para abrir este vídeo."
                                    } else {
                                        savedPositionMs = player?.currentPosition ?: savedPositionMs
                                        savedPlayWhenReady = player?.playWhenReady ?: savedPlayWhenReady
                                        externalLaunchPending = true
                                        player?.pause()
                                        try {
                                            currentActivity.startActivity(Intent.createChooser(viewIntent, "Abrir vídeo com"))
                                        } catch (_: ActivityNotFoundException) {
                                            externalLaunchPending = false
                                            if (savedPlayWhenReady) player?.play()
                                            externalOpenMessage = "Não foi possível iniciar uma aplicação compatível."
                                        } catch (error: Exception) {
                                            externalLaunchPending = false
                                            if (savedPlayWhenReady) player?.play()
                                            Log.e(TAG, "Falha ao iniciar o seletor de vídeo (${error.javaClass.simpleName})")
                                            externalOpenMessage = "O Android não conseguiu abrir o seletor de aplicações."
                                        }
                                    }
                                }
                            },
                        )
                    }
                    DropdownMenu(
                        expanded = presentationMenuExpanded,
                        onDismissRequest = { presentationMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ajustar imagem") },
                            onClick = { presentationMenuExpanded = false },
                        )
                        VideoPresentation.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                trailingIcon = {
                                    if (presentation == mode) Text("✓", color = MaterialTheme.colorScheme.primary)
                                },
                                onClick = {
                                    presentation = mode
                                    playerView?.resizeMode = mode.resizeMode
                                    presentationMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(116.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                playbackError?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = {
                        playbackError = null
                        if (validatePlaybackUrl(movie.videoUrl) != null) {
                            playbackError = "URL inválido ou esquema não suportado"
                        } else player?.apply {
                            stop()
                            setMediaItem(MediaItem.fromUri(movie.videoUrl), savedPositionMs)
                            prepare()
                            play()
                        }
                    }) { Text("Tentar novamente") }
                }
                externalOpenMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

private const val TAG = "TugaKidsPlayer"
private const val DEFAULT_CONTROLLER_TIMEOUT_MS = 5_000
private fun redactUrlQuery(message: String): String =
    message.replace(Regex("https?://[^\\s?]+\\?[^\\s]+"), "[url com parâmetros ocultos]")
