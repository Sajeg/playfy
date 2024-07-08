package com.sajeg.playfy.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.sajeg.playfy.SpotifyApi
import com.sajeg.playfy.SpotifySong
import com.sajeg.playfy.paddingModifier

@Composable
fun PlayListView(id: String) {
    var tracks by remember { mutableStateOf<List<SpotifySong>?>(null) }
    if (tracks == null) {
        Log.d("SpotifyApi", "true")
        SpotifyApi.getTracks(id, onDone = {
            tracks = it
        })
    }
    LazyColumn(
        modifier = paddingModifier.padding(horizontal = 5.dp)
    ) {
        if (tracks != null) {
            items(tracks!!) { track ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text(
                        text = track.title,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                    Text(
                        text = "from ${track.artist}",
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}