package com.iptv.player
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.iptv.player.data.PlaylistRepository
import com.iptv.player.model.Channel
import com.iptv.player.ui.screens.ChannelListScreen
import com.iptv.player.ui.screens.PlayerScreen
import com.iptv.player.ui.theme.IPTVPlayerTheme

class MainActivity : ComponentActivity() {
    private lateinit var repo: PlaylistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = PlaylistRepository(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            IPTVPlayerTheme {
                var currentChannel by remember { mutableStateOf<Channel?>(null) }
                var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
                var showPlayer by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showPlayer && currentChannel != null) {
                        PlayerScreen(channel = currentChannel, channelList = channels,
                            onBack = { showPlayer = false; currentChannel = null },
                            onChannelSelected = { ch -> currentChannel = ch; repo.saveLastChannel(ch.name) })
                    } else {
                        ChannelListScreen(onPlayChannel = { ch ->
                            currentChannel = ch; channels = repo.loadChannels(); repo.saveLastChannel(ch.name); showPlayer = true
                        }, repo = repo)
                    }
                }
            }
        }
    }
}
