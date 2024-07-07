package com.sajeg.playfy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.playfy.Playlist
import com.sajeg.playfy.paddingModifier
import com.sajeg.playfy.spotifyApi
import org.json.JSONObject


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var playlists by remember { mutableStateOf<List<Playlist>?>(null) }
    if (spotifyApi != null) {
        spotifyApi!!.getUsername { userName ->
            name = userName
        }
        if (playlists == null) {
            spotifyApi!!.getPlaylists {
                val outputPlaylists = mutableListOf<Playlist>()
                for (i in 0..<it.length()) {
                    val playlist = it[i] as JSONObject
                    val images = playlist.getJSONArray("images")[0] as JSONObject
                    outputPlaylists.add(
                        Playlist(
                            id = playlist.getString("id"),
                            name = playlist.getString("name"),
                            description = playlist.getString("description"),
                            imgUrl = images.getString("url"),
                            trackCount = playlist.getJSONObject("tracks").getInt("total")
                        )
                    )
                    playlists = outputPlaylists
                }
            }
        }
    }

    Column(
        modifier = paddingModifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Hi, $name",
            fontSize = 32.sp
        )
        LazyColumn {
            if (playlists != null) {
                for (playlist in playlists!!) {
                    item {
                        Column {
                            GlideImage(model = playlist.imgUrl, contentDescription = playlist.name)
                            Text(text = playlist.name)
                        }
                    }
                }
            }
        }
    }
}