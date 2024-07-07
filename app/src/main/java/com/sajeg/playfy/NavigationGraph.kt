package com.sajeg.playfy

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sajeg.playfy.screens.HomeScreen
import com.sajeg.playfy.screens.PlayListView
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
        composable<PlaylistScreen> {
            val id = it.toRoute<PlaylistScreen>().id
            PlayListView(id = id)
        }
    }
}

@Serializable
object HomeScreen

@Serializable
object SignInScreen

@Serializable
data class PlaylistScreen(
    val id: String
)