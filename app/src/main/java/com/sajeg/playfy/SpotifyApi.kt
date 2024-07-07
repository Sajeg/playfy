package com.sajeg.playfy

import android.util.Log
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class SpotifyApi(private val token: String) {

    fun getPlaylists() {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/playlists")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val jsonObject = JSONObject(it)
                        val playlists = jsonObject.getJSONArray("items")
                        for (i in 0 until playlists.length()) {
                            val playlist = playlists.getJSONObject(i)
                            val playlistName = playlist.getString("name")
                            Log.d("Playlist", playlistName)
                        }
                    }
                }
            }
        })
    }
}