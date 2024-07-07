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
import com.sajeg.playfy.SpotifyApi
import com.sajeg.playfy.SpotifySong
import com.sajeg.playfy.spotifyApi

@Composable
fun PlayListView(id: String) {
    var tracks by remember { mutableStateOf<List<SpotifySong>?>(null) }
    if (tracks == null) {
        Log.d("SpotifyApi", "true")
        SpotifyApi.getTracks(id, onDone = {
            tracks = it
        })
    }
    Column {
        Text(text = SpotifyApi.userName!!)
    }
    LazyColumn {
        if (tracks != null) {
            items(tracks!!) { track ->
                Text(text = "${track.title} from ${track.artist}")
            }
        }
    }
}