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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sajeg.playfy.GeminiApi
import com.sajeg.playfy.HomeScreen
import com.sajeg.playfy.R
import com.sajeg.playfy.Songs
import com.sajeg.playfy.SpotifyApi
import com.sajeg.playfy.SpotifySong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorScreen(navController: NavController, playlistId: String, title: String) {
    var tracks by remember { mutableStateOf<List<SpotifySong>?>(null) }
    val songs = mutableListOf<Songs>()
    val spotifyOutput by remember { mutableStateOf<MutableList<SpotifySong>>(mutableListOf()) }
    var requestSent by remember { mutableStateOf(false) }
    var showOutput by remember { mutableStateOf(false) }
    var num by remember { mutableIntStateOf(0) }

    if (!showOutput) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        Log.d("Gemini", "HErE")
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = title) },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.back),
                                contentDescription = ""
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    showOutput = false
                    for (song in spotifyOutput) {
                        SpotifyApi.addSong(song.id, playlistId)
                    }
                    navController.navigate(HomeScreen)
                }) {
                    Icon(painter = painterResource(id = R.drawable.check), contentDescription = "")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                Text(
                    text = "Add these songs to $title?",
                    modifier = Modifier.padding(15.dp),
                    fontSize = 20.sp
                )
                LazyColumn {
                    items(spotifyOutput) { track ->
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
        }
    }
    if (tracks == null) {
        SpotifyApi.getTracks(playlistId, onDone = {
            tracks = it
        })
    } else if (!requestSent) {
        requestSent = true
        for (song in tracks!!) {
            songs.add(song.toSongs())
        }
        LaunchedEffect(showOutput) {
            CoroutineScope(Dispatchers.IO).launch {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("Gemini", "Extending now")
                    val output =
                        GeminiApi.extendPlaylist(songs.toList(), playlistId) ?: return@launch
                    for (newSong in output) {
                        SpotifyApi.searchSong(newSong, onDone = {
                            Log.d("Gemini", "converted Song ")
                            spotifyOutput.add(it)
                            if (spotifyOutput.size >= 10) {
                                Log.d("Gemini", "Showing output")
                                showOutput = true
                            }
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