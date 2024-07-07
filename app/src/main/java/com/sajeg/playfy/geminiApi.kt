package com.sajeg.playfy

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart

data class songs(
    val title: String,
    val artist: String
)

object geminiApi {
    val modelPlaylistExtender = GenerativeModel(
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
        )
    )

    suspend fun extendPlaylist(playlist: List<songs>) {
        //To-Do
    }
}