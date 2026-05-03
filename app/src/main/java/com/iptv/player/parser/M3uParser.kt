package com.iptv.player.parser
import com.iptv.player.model.Channel
import com.iptv.player.model.Playlist
import com.iptv.player.model.PlaylistSource
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object M3uParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).followRedirects(true).build()

    fun parseFromUrl(url: String, callback: (Result<Playlist>) -> Unit) {
        Thread {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: throw Exception("Empty response")
                callback(Result.success(parseM3uContent(body, PlaylistSource.Url(url))))
            } catch (e: Exception) { callback(Result.failure(e)) }
        }.start()
    }

    fun parseFromFile(file: File): Playlist = parseM3uContent(FileReader(file).use { it.readText() }, PlaylistSource.Local(file.absolutePath))

    fun parseFromString(content: String): Playlist = parseM3uContent(content, PlaylistSource.Url(""))

    fun parseM3uContent(content: String, source: PlaylistSource): Playlist {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val commaIdx = line.lastIndexOf(',')
                val name = if (commaIdx != -1) line.substring(commaIdx + 1).trim() else ""
                val tvgId = Regex("""tvg-id="([^"]*)"""").find(line)?.groupValues?.getOrNull(1) ?: ""
                val logo = Regex("""tvg-logo="([^"]*)"""").find(line)?.groupValues?.getOrNull(1) ?: ""
                val group = Regex("""group-title="([^"]*)"""").find(line)?.groupValues?.getOrNull(1) ?: "未分类"
                val urls = mutableListOf<String>()
                i++
                while (i < lines.size) {
                    val next = lines[i].trim()
                    if (next.startsWith("#EXTINF:") || next.startsWith("#EXTM3U")) break
                    if (next.isNotEmpty() && !next.startsWith("#") && !next.startsWith("rtmp://")) urls.add(next)
                    i++
                }
                if (name.isNotEmpty() && urls.isNotEmpty()) {
                    // Clean name: remove [HD] [BD] etc prefix
                    val cleanName = name.replace(Regex("""^\[(HD|BD|SD|VGA)\]\s*"""), "")
                        .replace(Regex("""\s*\[geo-blocked\]"""), "")
                        .replace(Regex("""\s*\*[a-z0-9]+\s*$"""), "").trim()
                    channels.add(Channel(name = cleanName, logo = logo, group = group, urls = urls, tvgId = tvgId))
                }
            } else i++
        }
        return Playlist(channels = channels, source = source)
    }

    private class FileReader(file: File) {
        val readText: () -> String = { file.readText() }
    }
}
