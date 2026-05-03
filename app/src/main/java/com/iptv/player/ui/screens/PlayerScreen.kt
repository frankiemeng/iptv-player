package com.iptv.player.ui.screens
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.iptv.player.model.Channel
import com.iptv.player.ui.theme.*

@Composable
fun PlayerScreen(channel: Channel?, channelList: List<Channel>, onBack: () -> Unit, onChannelSelected: (Channel) -> Unit) {
    var currentChannel by remember { mutableStateOf(channel) }
    var showPlaylist by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    
    LaunchedEffect(player) {
        player.playWhenReady = true
    }

    LaunchedEffect(currentChannel) {
        currentChannel?.let {
            val mediaItem = MediaItem.fromUri(Uri.parse(it.primaryUrl))
            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = this@PlayerScreen.player
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                Surface(modifier = Modifier.align(Alignment.TopStart).padding(12.dp), color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp)) {
                    Text(currentChannel?.name ?: "", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    Icon(Icons.Filled.Close, "返回", tint = Color.White)
                }
                IconButton(onClick = { showPlaylist = !showPlaylist }, modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 8.dp)) {
                    Icon(if (showPlaylist) Icons.Filled.List else Icons.Filled.FormatListBulletedAdd, "频道列表", tint = Color.White)
                }
                Column(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val idx = channelList.indexOfFirst { it.name == currentChannel?.name }
                    IconButton(onClick = { onChannelSelected(channelList[(idx - 1 + channelList.size) % channelList.size]) },
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.5f))) {
                        Icon(Icons.Filled.KeyboardArrowUp, "上一个", tint = Color.White)
                    }
                    IconButton(onClick = { onChannelSelected(channelList[(idx + 1) % channelList.size]) },
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.5f))) {
                        Icon(Icons.Filled.KeyboardArrowDown, "下一个", tint = Color.White)
                    }
                }
            }
        }

        if (showPlaylist) {
            Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground.copy(alpha = 0.95f)) {
                Column {
                    Surface(modifier = Modifier.fillMaxWidth(), color = DarkSurface) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("频道列表", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("${channelList.size} 个频道", color = TextSecondary, fontSize = 12.sp)
                            IconButton(onClick = { showPlaylist = false }) { Icon(Icons.Filled.Close, "关闭", tint = TextSecondary) }
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        items(channelList) { ch ->
                            val isCurrent = ch.name == currentChannel?.name
                            Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                                currentChannel = ch; onChannelSelected(ch); showPlaylist = false
                            }, color = if (isCurrent) PrimaryBlue.copy(alpha = 0.15f) else DarkSurface, shape = RoundedCornerShape(10.dp)) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (isCurrent) { Icon(Icons.Filled.PlayArrow, "播放中", tint = PrimaryBlue, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
                                    Text(ch.name, color = if (isCurrent) PrimaryBlue else TextPrimary, fontSize = 14.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.weight(1f))
                                    Text(ch.group, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
