package com.iptv.player.data
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iptv.player.model.Channel

class PlaylistRepository(context: Context) {
    private val prefs = context.getSharedPreferences("iptv_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveChannels(channels: List<Channel>) {
        prefs.edit().putString("channels", gson.toJson(channels)).apply()
    }

    fun loadChannels(): List<Channel> {
        val json = prefs.getString("channels", null) ?: return emptyList()
        return try { gson.fromJson(json, object : TypeToken<List<Channel>>() {}.type) } catch (e: Exception) { emptyList() }
    }

    fun saveUrl(url: String) {
        val urls = getSavedUrls().toMutableList()
        if (!urls.contains(url)) { urls.add(0, url); if (urls.size > 20) urls.removeAt(urls.lastIndex); prefs.edit().putString("saved_urls", gson.toJson(urls)).apply() }
    }

    fun getSavedUrls(): List<String> {
        val json = prefs.getString("saved_urls", null) ?: return emptyList()
        return try { gson.fromJson(json, object : TypeToken<List<String>>() {}.type) } catch (e: Exception) { emptyList() }
    }

    fun saveLastChannel(name: String) { prefs.edit().putString("last_channel", name).apply() }
    fun getLastChannel(): String = prefs.getString("last_channel", "") ?: ""
}
