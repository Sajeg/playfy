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
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AccountsQueryParameters.REDIRECT_URI
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

var paddingModifier = Modifier.padding(0.dp)
var clientId = BuildConfig.clientId

class MainActivity : ComponentActivity() {
    private val redirectUri = "playfy://callback"
    private val REQUEST_CODE = 1337
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

                        connectSpotifyAppRemote()
                    }

                    AuthorizationResponse.Type.ERROR -> {
                        Log.e("SpotifyAuth", "Auth error: " + response.error)
                    }

                    else -> {
                        Log.d("SpotifyAuth", "Auth result: " + response.type)
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
                    paddingModifier = paddingModifier.padding(innerPadding)
                }
            }
        }
        authenticateSpotify()
        // connectSpotifyAppRemote()
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

    private fun connectSpotifyAppRemote() {
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                Log.d("SpotifyAuth", "Connected! Yay!")
                // Now you can start using the SpotifyAppRemote
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyAuth", throwable.message, throwable)
            }
        })
    }

    private fun connected() {
        spotifyAppRemote?.let {
            // Play a playlist
            it.playerApi.pause()
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d("MainActivity", track.name + " by " + track.artist.name)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }

    }
}
