package com.example.sos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.sos.ui.theme.SOSTheme
import com.example.sos.ui.theme.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SOSTheme {
                val auth = remember { FirebaseAuth.getInstance() }
                var currentScreen by remember { mutableStateOf("splash") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "splash" -> SplashScreen(onVideoEnd = {
                            currentScreen = "loading"
                        })
                        "loading" -> LoadingScreen(onTimeout = {
                            val currentUser = auth.currentUser
                            currentScreen = if (currentUser != null) "dashboard" else "login"
                        })
                        "login" -> LoginScreen(onLoginSuccess = {
                            currentScreen = "dashboard"
                        })
                        "dashboard" -> DashboardScreen(onLogout = {
                            auth.signOut()
                            currentScreen = "login"
                        })
                    }
                }
            }
        }
    }
}

/**
 * Plays splash.mp4 once and then transitions.
 */
@Composable
fun SplashScreen(onVideoEnd: () -> Unit) {
    VideoPlayerScreen(videoResId = R.raw.splash, loop = false, onVideoEnd = onVideoEnd)
}

/**
 * Plays loading.mp4 looping for 3 seconds and then transitions.
 */
@Composable
fun LoadingScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }
    VideoPlayerScreen(videoResId = R.raw.loading, loop = true)
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    videoResId: Int,
    loop: Boolean = false,
    onVideoEnd: () -> Unit = {}
) {
    val context = LocalContext.current
    val onVideoEndStable by rememberUpdatedState(onVideoEnd)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().also { player ->
            val uri = "android.resource://${context.packageName}/$videoResId"
            player.setMediaItem(MediaItem.fromUri(uri))
            player.volume = 0f
            player.repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            player.prepare()
            player.playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onVideoEndStable()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            }
        )
    }
}
