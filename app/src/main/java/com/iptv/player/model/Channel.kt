package com.iptv.player.model

data class Channel(
    val name: String,
    val logo: String = "",
    val group: String = "未分类",
    val urls: List<String> = emptyList(),
    val tvgId: String = ""
) {
    val primaryUrl: String get() = urls.firstOrNull() ?: ""
}

data class Playlist(
    val title: String = "",
    val channels: List<Channel> = emptyList(),
    val source: PlaylistSource = PlaylistSource.Url("")
)

sealed class PlaylistSource {
    data class Url(val url: String) : PlaylistSource()
    data class Local(val path: String) : PlaylistSource()
}
