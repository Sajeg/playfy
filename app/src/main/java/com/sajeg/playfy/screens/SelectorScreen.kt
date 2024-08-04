package com.sajeg.playfy.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sajeg.playfy.GeminiApi
import com.sajeg.playfy.Songs
import com.sajeg.playfy.SpotifyApi
import com.sajeg.playfy.SpotifySong
import com.sajeg.playfy.SpotifySongList
import com.sajeg.playfy.toJsonString
import com.sajeg.playfy.toSpotifySongList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SelectorScreen(navController: NavController, playlistId: String) {
    var tracks by remember { mutableStateOf<List<SpotifySong>?>(null) }
    val songs = mutableListOf<Songs>()
    val spotifyOutput by remember { mutableStateOf<MutableList<SpotifySong>?>(null) }
    var requestSent by remember { mutableStateOf(false) }
    var num by remember { mutableIntStateOf(0) }

    if (tracks == null || spotifyOutput == null) {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            CircularProgressIndicator()
        }
    } else if (spotifyOutput != null){
        LazyColumn {
            items(spotifyOutput!!) { track ->
                num++
                Card(
                    colors = CardColors(
                        containerColor = if (num % 2 == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (num % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.primary
                    ),
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
    if (tracks == null) {
        SpotifyApi.getTracks(playlistId, onDone = {
            tracks = it
        })
    } else if (!requestSent){
        requestSent = true
        for (song in tracks!!) {
            songs.add(song.toSongs())
        }
        LaunchedEffect(spotifyOutput) {
            CoroutineScope(Dispatchers.IO).launch {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("Gemini", "Extending now")
                    val output = GeminiApi.extendPlaylist(songs.toList(), playlistId)
                    Log.d("Gemini", output.toString())
                    if (output == null) {
                        return@launch
                    }
                    for (newSong in output) {
                        SpotifyApi.searchSong(newSong, onDone = {
                            Log.d("Gemini", "converted Song ")
                            spotifyOutput?.add(it)
                        })
                    }
                }
            }
        }
    }
//    LazyColumn {
//        items(songs) { song ->
//            Text(text = song.title)
//        }
//    }
}