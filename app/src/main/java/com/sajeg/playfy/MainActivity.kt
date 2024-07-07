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
    private lateinit var navController: NavHostController

    private val spotifyAuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val response = AuthorizationClient.getResponse(result.resultCode, result.data)
                when (response.type) {
                    AuthorizationResponse.Type.TOKEN -> {
                        val token = response.accessToken
                        Log.d("SpotifyAuth", "Token: $token")
                        spotifyApi = SpotifyApi(token)
                        connectSpotifyAppRemote(token)
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

        builder.setScopes(
            arrayOf(
                "playlist-read-private",
                "playlist-read-collaborative",
                "playlist-modify-private",
                "playlist-modify-public",
                "streaming"
            )
        )
        val request = builder.build()

        val intent = AuthorizationClient.createLoginActivityIntent(this, request)
        spotifyAuthLauncher.launch(intent)
    }

    private fun connectSpotifyAppRemote(token: String) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("SpotifyAuth", "Connected! Yay!")
                onSpotifyConnected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyAuth", "Connection failed: ${throwable.message}", throwable)
            }
        })
    }

    private fun onSpotifyConnected() {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.pause()

            remote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                val track: Track = playerState.track
                Log.d("MainActivity", "${track.name} by ${track.artist.name}")
            }
            navController.navigate(HomeScreen)
        }

        CoroutineScope(Dispatchers.IO).launch {
            GeminiApi.extendPlaylist(
                listOf(
                    Songs("In the end", "Linkin Park"),
                    Songs("A murder of Crows", "Sum 41")
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }
}
