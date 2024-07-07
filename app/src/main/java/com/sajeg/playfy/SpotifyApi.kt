package com.sajeg.playfy

import android.util.Log
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
                Log.d("PlaylistContent", e.toString())
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
                                val artist = artists[j] as JSONObject
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
                        Log.d("PlaylistContent", songs.toString())
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

    fun searchSong(song: Songs, onDone: (track: SpotifySong) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(
                "https://api.spotify.com/v1/search?${
                    song.title.replace(
                        " ",
                        "+"
                    )
                }+${song.artist.replace(" ", "+")}&type=track"
            )
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
                        val tracks = jsonObject.getJSONArray("items")[0] as JSONObject
                        val artists = tracks.getJSONArray("artists")
                        var artistsString = ""
                        for (j in 0..<artists.length()) {
                            val artist = artists[j] as JSONObject
                            artistsString += artist.getString("name")
                        }
                        onDone(
                            SpotifySong(
                                title = tracks.getString("name"),
                                artist = tracks.getString("artist"),
                                id = tracks.getString("id")
                            )
                        )
                    }
                }
            }
        })
    }
}