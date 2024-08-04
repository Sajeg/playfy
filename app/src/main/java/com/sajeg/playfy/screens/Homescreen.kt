package com.sajeg.playfy.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.playfy.GeminiApi
import com.sajeg.playfy.Playlist
import com.sajeg.playfy.PlaylistScreen
import com.sajeg.playfy.R
import com.sajeg.playfy.SelectorScreen
import com.sajeg.playfy.SpotifyApi
import com.sajeg.playfy.paddingModifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var playlists by remember { mutableStateOf<List<Playlist>?>(null) }
    var sentRequest by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            try {
                name = ""
                sentRequest = false
                pullToRefreshState.endRefresh()
            } catch (e: Exception) {
                pullToRefreshState.endRefresh()
            }
        }
    }

    if (SpotifyApi.token != "" && name == "") {
        SpotifyApi.getUsername { username ->
            name = username
            if (!sentRequest) {
                SpotifyApi.getPlaylists {
                    val outputPlaylists = mutableListOf<Playlist>()
                    for (i in 0..<it.length()) {
                        val playlist = it[i] as JSONObject
                        val images = try {
                             playlist.getJSONArray("images")[0] as JSONObject
                        } catch (_:Exception) {
                            null
                        }
                        if (images != null) {
                            outputPlaylists.add(
                                Playlist(
                                    id = playlist.getString("id"),
                                    name = playlist.getString("name"),
                                    description = playlist.getString("description"),
                                    imgUrl = images.getString("url"),
                                    trackCount = playlist.getJSONObject("tracks").getInt("total")
                                )
                            )
                        }
                    }
                    playlists = outputPlaylists
                }
                sentRequest = true
            }
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showDialog = true;
            }) {
                Icon(painter = painterResource(id = R.drawable.add), contentDescription = "")
            }
        }
    ) { padding ->
        AnimatedVisibility(showDialog) {
            var prompt by remember { mutableStateOf("") }
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        navController.navigate(SelectorScreen(prompt = prompt,
                            playlistId = null, title = null
                        ))
                    }
                    ) {
                        Text(text = "Confirm")
                    }
                },
                title = { Text(text = "New Playlist") },
                text = {
                    TextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Specify a genre, topic, artist..."
                            )
                        })
                })
        }
        Column(
            modifier = paddingModifier.padding(horizontal = 20.dp).nestedScroll(pullToRefreshState.nestedScrollConnection),
        ) {
            Text(
                text = "Hi, $name",
                fontSize = 32.sp
            )
            Text(text = "Select a Playlist, that you want to modify")
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 20.dp),
            ) {
                playlists?.let { list ->
                    items(list) { playlist ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GlideImage(
                                model = playlist.imgUrl,
                                contentDescription = playlist.name,
                                modifier = Modifier
                                    .size(150.dp)
                                    .aspectRatio(1f)
                                    .clickable {
                                        navController.navigate(
                                            PlaylistScreen(
                                                playlist.id,
                                                playlist.name,
                                                playlist.imgUrl
                                            )
                                        )
                                    }
                            )
                            Text(text = playlist.name)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PullToRefreshContainer(
                modifier = Modifier,
                state = pullToRefreshState,
            )
        }
    }
}