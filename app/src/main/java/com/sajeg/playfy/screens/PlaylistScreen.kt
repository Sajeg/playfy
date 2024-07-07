package com.sajeg.playfy.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sajeg.playfy.SpotifySong
import com.sajeg.playfy.spotifyApi

@Composable
fun PlayListView(id: String) {
    val api = spotifyApi
    var tracks by remember { mutableStateOf<List<SpotifySong>?>(null) }
    if (api != null) {
        Log.d("SpotifyApi", "true")
        spotifyApi!!.getTracks(id, onDone = {
            tracks = it
        })
    }
    Column {
        Text(text = api!!.userName!!)
    }
    LazyColumn {
        if (tracks != null) {
            items(tracks!!) { track ->
                Text(text = "${track.title} from ${track.artist}")
            }
        }
    }
}