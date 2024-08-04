package com.sajeg.playfy

import android.util.Log
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

object SpotifyApi {
    var token = ""
    var userName: String? = null
    var userId: String? = null
    var playlists: JSONArray? = null

    fun getPlaylists(onDone: (playlists: JSONArray) -> Unit) {
        Log.d("Requesting...", "Playlist")
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/users/$userId/playlists")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("ErrorResponse", "Error")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        Log.d("SuccessfulResponse", it)
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
            .url("https://api.spotify.com/v1/playlists/$id/tracks")
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
                        userId = jsonObject.getString("id")
                        userName = jsonObject.getString("display_name")
                        onDone(userName!!)
                    }
                }
            }
        })
    }

    fun searchSong(song: Songs, onDone: (track: SpotifySong) -> Unit) {
        val client = OkHttpClient()
        val encodedQuery = URLEncoder.encode("${song.title} ${song.artist}", "UTF-8")
        val request = Request.Builder()
            .url(
                "https://api.spotify.com/v1/search?q=$encodedQuery&type=track"
            )
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body?.string()
                        responseBody?.let {
                            val jsonObject = JSONObject(it)
                            val tracks = jsonObject.getJSONObject("tracks")
                                .getJSONArray("items")[0] as JSONObject
                            val artists = tracks.getJSONArray("artists")
                            var artistsString = ""
                            for (j in 0..<artists.length()) {
                                val artist = artists[j] as JSONObject
                                artistsString += artist.getString("name")
                            }
                            Log.d("SpotifyApi", "one success")
                            onDone(
                                SpotifySong(
                                    title = tracks.getString("name"),
                                    artist = artistsString,
                                    id = tracks.getString("id")
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.d("SpotifyAPI", e.toString())
                    }
                }
            }
        })
    }

    fun addSong(trackId: String, id: String) {
        val client = OkHttpClient()
        val uri = "spotify:track:$trackId"
        val encodedUri = URLEncoder.encode(uri, "UTF-8")


        val request = Request.Builder()
            .url(
                "https://api.spotify.com/v1/playlists/$id/tracks?uris=$encodedUri"
            )
            .post(FormBody.Builder().build())
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Success", "Successfully extended Playlist")
                }
            }
        })
    }

    fun createPlaylist(title: String, onDone: (playlistId: String) -> Unit) {
        val client = OkHttpClient()
        val body = """{"name": "$title"}""".trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(
                "https://api.spotify.com/v1/users/$userId/playlists"
            )
            .post(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Authorization", "Bearer $token")
            .build()
        Log.d("SpotifyApi", "Sending request")

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("SpotifyApi", "ERROR ${e.localizedMessage}")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Success", "Successfully created Playlist")
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val jsonObject = JSONObject(it)
                        onDone(jsonObject.getString("id"))
                    }
                } else {
                    Log.e("SpotifyApi", "Error: ${response.networkResponse}")
                }
            }
        })
    }
}