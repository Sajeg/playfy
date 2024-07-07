package com.sajeg.playfy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sajeg.playfy.ui.theme.PlayfyTheme
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var paddingModifier = Modifier.padding(0.dp)
var spotifyApi: SpotifyApi? = null

class MainActivity : ComponentActivity() {
    private val clientId = BuildConfig.clientId
    private val redirectUri = "playfy://callback"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var spotifyToken: String? = null
    private lateinit var navController: NavHostController

    private val spotifyAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val response = AuthorizationClient.getResponse(result.resultCode, result.data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    spotifyToken = response.accessToken
                    Log.d("SpotifyAuth", "Token: $spotifyToken")
                    onSpotifyAuthenticated()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("SpotifyAuth", "Auth error: ${response.error}")
                }
                else -> {
                    Log.d("SpotifyAuth", "Auth result: ${response.type}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlayfyTheme {
                navController = rememberNavController()
                SetupNavGraph(navController = navController)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    paddingModifier = Modifier.padding(innerPadding)
                    SetupNavGraph(navController = navController)
                }
            }
        }
        authenticateSpotify()
    }

    private fun authenticateSpotify() {
        val builder = AuthorizationRequest.Builder(
            clientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri
        )
        builder.setScopes(arrayOf(
            "playlist-read-private",
            "playlist-read-collaborative",
            "playlist-modify-private",
            "playlist-modify-public",
            "user-library-read",
            "user-read-private",
            "user-read-email"
        ))
        val request = builder.build()
        val intent = AuthorizationClient.createLoginActivityIntent(this, request)
        spotifyAuthLauncher.launch(intent)
    }

    private fun onSpotifyAuthenticated() {
        // Here you can start using the Spotify Web API with the token
        Log.d("SpotifyAuth", "Authenticated successfully!")

        // Example: Use the token to make API requests
        spotifyToken?.let { token ->
            SpotifyApi.token = token
            CoroutineScope(Dispatchers.Main).launch {
                navController.navigate(HomeScreen)
            }
        }
    }
}
