package com.sajeg.playfy

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class SpotifyApi(private val token: String) {
    var userName: String? = null
    var playlists: JSONArray? = null

    fun getPlaylists(onDone: (playlists: JSONArray) -> Unit) {
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
                        playlists = jsonObject.getJSONArray("items")
                        onDone(jsonObject.getJSONArray("items"))
                    }
                }
            }
        })
    }

    fun getTracks(id: String, onDone: (tracks: List<SpotifySong>) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/{$id}/tracks")
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
                        val tracks = jsonObject.getJSONArray("items")
                        val songs = mutableListOf<SpotifySong>()
                        for (i in 0..<tracks.length()) {
                            val metaData = tracks[i] as JSONObject
                            val track = metaData.getJSONObject("track")
                            val artists = track.getJSONArray("artists")
                            var artistsString = ""
                            for (j in 0..<artists.length()) {
                                val artist = artists[i] as JSONObject
                                artistsString += artist.getString("name")
                            }

                            songs.add(
                                SpotifySong(
                                    id = track.getString("id"),
                                    title = track.getString("name"),
                                    artist = artistsString
                                )
                            )
                        }
                        onDone(songs.toList())
                    }
                }
            }
        })
    }

    fun getUsername(onDone: (userName: String) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
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
                        userName = jsonObject.getString("display_name")
                        onDone(userName!!)
                    }
                }
            }
        })
    }
}