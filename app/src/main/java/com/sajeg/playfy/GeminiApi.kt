package com.sajeg.playfy

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.json.Json
import org.json.JSONObject


object GeminiApi {
    private val modelPlaylistExtender = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.geminiApiKey,
        systemInstruction = Content(
            role = "user", parts = listOf(
                TextPart(
                    "You'll receive a list of songs. Your task is then to add 10 songs with " +
                            "their title and artist that are similar to the songs, that are already in the playlist with a good mix between popular and unknown songs. " +
                            "Do not return the Songs, that you received, but also the genre of the Playlist. Not for each song individually."
                )
            )
        ),
        safetySettings = listOf(
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE)
        ),
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    private val modelPlaylistCreator = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.geminiApiKey,
        systemInstruction = Content(
            role = "user", parts = listOf(
                TextPart(
                    "You'll receive a set of criteria and your task is then to give back 25 songs with " +
                            "their title and artist that meet the criteria. Make a good mix between popular and unknown songs. " +
                            "Also return the a name for the Playlist like called playlistName."
                )
            )
        ),
        safetySettings = listOf(
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE)
        ),
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    suspend fun extendPlaylist(playlist: List<Songs>, id: String): List<Songs>? {
        val response = modelPlaylistExtender.generateContent(playlist.toString())
        val text = response.candidates[0].content.parts[0].asTextOrNull()
        if (text != null) {
            val output = JSONObject(text)
            val songs = Json.decodeFromString<List<Songs>>(output.getJSONArray("songs").toString())
            Log.d("Gemini", "Successfully converted!")
            return songs
        } else {
            Log.d("Gemini", "Not successfully converted!")
            return null
        }
    }

    suspend fun newPlaylist(prompt: String): Pair<List<Songs>?, String> {
        val response = modelPlaylistCreator.generateContent(prompt)
        val text = response.candidates[0].content.parts[0].asTextOrNull()
        Log.d("Response", text!!)
        if (text != null) {
            val output = JSONObject(text)
            val songs = Json.decodeFromString<List<Songs>>(output.getJSONArray("songs").toString())
            return Pair(songs, output.getString("playlistName"))
        } else {
            return Pair(null, "n/a")
        }
    }
}