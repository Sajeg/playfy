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