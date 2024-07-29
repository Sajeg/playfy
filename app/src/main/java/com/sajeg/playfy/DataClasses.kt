package com.sajeg.playfy

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

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

@Serializable
data class SpotifySong(
    val title: String,
    val artist: String,
    val id: String
) {
    fun toSongs() : Songs{
        return Songs(
            title = title,
            artist = artist
        )
    }
}

@Serializable
data class SpotifySongList(
    val songs: List<SpotifySong>
)

fun List<SpotifySong>.toJsonString(): String {
    return Json.encodeToString(ListSerializer(SpotifySong.serializer()), this)
}

fun String.toSpotifySongList(): List<SpotifySong> {
    return Json.decodeFromString(ListSerializer(SpotifySong.serializer()), this)
}