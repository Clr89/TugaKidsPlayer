package com.clr89.tugakidsplayer.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.clr89.tugakidsplayer.data.model.Movie

@Composable
fun PlayerScreen(movie: Movie, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    var player by remember(movie.id) { mutableStateOf<ExoPlayer?>(null) }
    var playbackError by remember(movie.id) { mutableStateOf<String?>(null) }
    var resumeAfterLifecyclePause by remember(movie.id) { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, movie.id, movie.videoUrl) {
        fun createPlayer() {
            if (player != null) return
            playbackError = null
            player = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        playbackError = "Não foi possível reproduzir este vídeo. Verifica a ligação e tenta novamente."
                    }
                })
                setMediaItem(MediaItem.fromUri(movie.videoUrl))
                prepare()
                playWhenReady = true
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> createPlayer()
                Lifecycle.Event.ON_PAUSE -> {
                    resumeAfterLifecyclePause = player?.isPlaying == true
                    player?.pause()
                }
                // Releasing at STOP frees the decoder; START creates a fresh player if the screen returns.
                Lifecycle.Event.ON_STOP -> {
                    player?.release()
                    player = null
                }
                Lifecycle.Event.ON_RESUME -> if (resumeAfterLifecyclePause) {
                    player?.play()
                    resumeAfterLifecyclePause = false
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
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack) { Text("Voltar ao catálogo") }
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(text = movie.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = "Vídeo de demonstração CC0", style = MaterialTheme.typography.bodySmall)
                }
            }

            // PlayerView's FIT resize mode preserves the stream's aspect ratio with letterboxing.
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    .background(androidx.compose.ui.graphics.Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                val activePlayer = player
                if (activePlayer != null) {
                    AndroidView(
                        factory = { viewContext ->
                            PlayerView(viewContext).apply {
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                useController = true
                                player = activePlayer
                            }
                        },
                        update = { view -> view.player = activePlayer },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            playbackError?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
                Button(
                    onClick = {
                        playbackError = null
                        player?.apply {
                            stop()
                            setMediaItem(MediaItem.fromUri(movie.videoUrl))
                            prepare()
                            play()
                        }
                    },
                ) {
                    Text("Tentar novamente")
                }
            }
        }
    }
}
