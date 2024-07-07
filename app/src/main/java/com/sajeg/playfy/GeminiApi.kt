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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


object GeminiApi {
    private val modelPlaylistExtender = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.geminiApiKey,
        systemInstruction = Content(
            role = "user", parts = listOf(
                TextPart("You'll receive a list of songs. You task is then to add 50 songs with " +
                        "their title and artist that would fit the songs, that are already in the playlist")
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

    suspend fun extendPlaylist(playlist: List<Songs>) : List<Songs> {
        val response = modelPlaylistExtender.generateContent(playlist.toString())
        val text = response.candidates[0].content.parts[0].asTextOrNull().toString()
        val output = Json.decodeFromString<List<Songs>>(text)
        Log.d("response", output.toString())

        return output
    }
}