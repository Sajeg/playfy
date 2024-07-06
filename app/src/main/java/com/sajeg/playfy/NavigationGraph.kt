package com.sajeg.playfy

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sajeg.playfy.screens.HomeScreen
import kotlinx.serialization.Serializable

@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    NavHost(navController = navController, startDestination = HomeScreen) {
        composable<HomeScreen> {
            HomeScreen(navController)
        }
    }
}

@Serializable
object HomeScreen