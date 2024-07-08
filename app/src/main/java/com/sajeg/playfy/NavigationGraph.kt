package com.sajeg.playfy

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sajeg.playfy.screens.HomeScreen
import com.sajeg.playfy.screens.PlayListView
import com.sajeg.playfy.screens.SelectorScreen
import com.sajeg.playfy.screens.SignInScreen
import kotlinx.serialization.Serializable

@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    NavHost(navController = navController, startDestination = SignInScreen) {
        composable<HomeScreen> {
            HomeScreen(navController)
        }
        composable<SignInScreen> {
            SignInScreen()
        }
        composable<SelectorScreen> {
            val params = it.toRoute<SelectorScreen>()
            SelectorScreen(navController = navController, songString = params.tracks)
        }
        composable<PlaylistScreen> {
            val params = it.toRoute<PlaylistScreen>()
            PlayListView(id = params.id, params.title, params.imgUrl, navController)
        }
    }
}

@Serializable
object HomeScreen

@Serializable
object SignInScreen

@Serializable
data class SelectorScreen(
    val tracks: SpotifySongList
)

@Serializable
data class PlaylistScreen(
    val id: String,
    val title: String,
    val imgUrl: String
)