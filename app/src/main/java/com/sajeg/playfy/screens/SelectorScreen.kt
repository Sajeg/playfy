package com.sajeg.playfy.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sajeg.playfy.SpotifySong
import com.sajeg.playfy.SpotifySongList
import com.sajeg.playfy.toSpotifySongList

@Composable
fun SelectorScreen(navController: NavController, songString: String) {
    val tracks = SpotifySongList(songString.toSpotifySongList())
    Log.d("SelectorScreen", tracks.toString())
//    LazyColumn {
//        items(songs) { song ->
//            Text(text = song.title)
//        }
//    }
}