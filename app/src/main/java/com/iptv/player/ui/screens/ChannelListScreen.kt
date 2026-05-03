package com.iptv.player.ui.screens
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.iptv.player.model.Channel
import com.iptv.player.parser.M3uParser
import com.iptv.player.data.PlaylistRepository
import com.iptv.player.ui.theme.*
import com.iptv.player.ui.components.SearchBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(onPlayChannel: (Channel) -> Unit, repo: PlaylistRepository) {
    val context = LocalContext.current
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var groups by remember { mutableStateOf<Map<String, List<Channel>>>(emptyMap()) }
    var selectedGroup by remember { mutableStateOf<String?>("全部") }
    var searchQuery by remember { mutableStateOf("") }
    var showUrlDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        channels = repo.loadChannels()
        groups = channels.groupBy { it.group }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
            Surface(modifier = Modifier.fillMaxWidth(), color = DarkSurface, shadowElevation = 4.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("小艺IPTV", color = PrimaryBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(if (channels.isEmpty()) "暂无频道，请添加源" else "${channels.size} 个频道", color = TextSecondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = { channels = repo.loadChannels(); groups = channels.groupBy { it.group } }) {
                        Icon(Icons.Filled.Refresh, "刷新", tint = TextSecondary)
                    }
                    IconButton(onClick = { showUrlDialog = true }) {
                        Icon(Icons.Filled.Add, "添加源", tint = PrimaryBlue, modifier = Modifier.size(26.dp))
                    }
                }
            }

            if (channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.LiveTv, null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(72.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("还没有频道", color = TextSecondary, fontSize = 18.sp)
                        Text("添加播放源开始观看", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp)
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(onClick = { showUrlDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f))) {
                            Icon(Icons.Filled.Link, "URL", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("从URL添加")
                        }
                    }
                }
            } else {
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                
                val groupNames = remember(groups) { listOf("全部") + groups.keys.sorted() }
                LazyRow(modifier = Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(groupNames) { group ->
                        val isSelected = selectedGroup == group
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGroup = group },
                            label = {
                                Text("$group${if (group != "全部") " (${groups[group]?.size ?: 0})" else " (${channels.size})"}", fontSize = 12.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = DarkSurfaceVariant,
                                selectedBorderColor = PrimaryBlue.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                val filtered = remember(channels, selectedGroup, searchQuery) {
                    var list = channels
                    if (selectedGroup != "全部") list = list.filter { it.group == selectedGroup }
                    if (searchQuery.isNotBlank()) list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    list
                }

                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filtered) { ch -> ChannelItem(channel = ch, onClick = { onPlayChannel(ch) }) }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showUrlDialog) {
        var url by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showUrlDialog = false }, containerColor = DarkSurface, shape = RoundedCornerShape(20.dp),
            title = { Text("添加播放源") },
            text = {
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("M3U URL") },
                    placeholder = { Text("https://example.com/playlist.m3u") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = DarkSurfaceVariant,
                        focusedLabelColor = PrimaryBlue, cursorColor = PrimaryBlue))
            },
            confirmButton = {
                Button(onClick = {
                    if (url.isNotBlank()) {
                        repo.saveUrl(url)
                        M3uParser.parseFromUrl(url) { result ->
                            result.onSuccess { playlist ->
                                repo.saveChannels(playlist.channels)
                                channels = playlist.channels
                                groups = playlist.channels.groupBy { it.group }
                            }
                        }
                    }
                    showUrlDialog = false
                }, enabled = url.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) { Text("确认添加") }
            },
            dismissButton = { TextButton(onClick = { showUrlDialog = false }) { Text("取消") } })
    }
}

@Composable
private fun ChannelItem(channel: Channel, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Text(channel.name.take(2), color = PrimaryBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(channel.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${channel.group} · ${channel.urls.size} 个源", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Filled.PlayCircle, "播放", tint = PrimaryBlue.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
        }
    }
}
