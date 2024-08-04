package com.sajeg.playfy.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.gson.Gson
import com.sajeg.playfy.GeminiApi
import com.sajeg.playfy.R
import com.sajeg.playfy.SelectorScreen
import com.sajeg.playfy.Songs
import com.sajeg.playfy.SpotifyApi
import com.sajeg.playfy.SpotifySong
import com.sajeg.playfy.SpotifySongList
import com.sajeg.playfy.toJsonString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun PlayListView(id: String, title: String, imgUrl: String, navController: NavController) {
    var tracks by remember { mutableStateOf<List<SpotifySong>?>(null) }
    var num by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    if (tracks == null) {
        SpotifyApi.getTracks(id, onDone = {
            tracks = it
        })
    }
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
                actions = {
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "https://open.spotify.com/playlist/$id"
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            type = "text/plain"
                        }

                        val shareIntent =
                            Intent.createChooser(sendIntent, "Share the Playlist")
                        ContextCompat.startActivity(
                            context,
                            shareIntent,
                            null
                        )
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.share),
                            contentDescription = ""
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(SelectorScreen(id, title))
            }) {
                Icon(painter = painterResource(id = R.drawable.add), contentDescription = "")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 5.dp)
        ) {
            item {
                GlideImage(
                    model = imgUrl,
                    contentDescription = "Playlist",
                    modifier = Modifier
                        .size(300.dp)
                        .padding(20.dp)
                )
            }
            if (tracks != null) {
                items(tracks!!) { track ->
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