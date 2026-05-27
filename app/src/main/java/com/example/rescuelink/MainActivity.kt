package com.example.rescuelink

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
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.rescuelink.ui.theme.SOSTheme
import com.example.rescuelink.ui.theme.LoginScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // True edge-to-edge — content draws behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            SOSTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = false // white icons on dark background
                    )
                }
                val auth = remember { FirebaseAuth.getInstance() }
                var currentScreen by remember { mutableStateOf("splash") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "splash" -> SplashScreen(onVideoEnd = {
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
 * Plays splash.mp4 once and then transitions to the auth gate.
 */
@Composable
fun SplashScreen(onVideoEnd: () -> Unit) {
    VideoPlayerScreen(videoResId = R.raw.splash, loop = false, onVideoEnd = onVideoEnd)
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
            .background(Color(0xFF090909))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    // Keep original format (fit) instead of stretching (fill)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setBackgroundColor(android.graphics.Color.parseColor("#090909"))
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            }
        )
    }
}
