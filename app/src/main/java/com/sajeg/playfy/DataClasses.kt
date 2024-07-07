package com.sajeg.playfy

import kotlinx.serialization.Serializable

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val imgUrl: String,
    val trackCount: Int,
)

@Serializable
data class Songs(
    val title: String,
    val artist: String
)

data class SpotifySong(
    val title: String,
    val artist: String,
    val id: String
)