package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.screens.DetailsScreen
import com.example.myapplication.screens.HomeScreens

@Composable
fun MovieNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MovieScreens.HomeScreen.name
    ) {
        composable(MovieScreens.HomeScreen.name) {
            HomeScreens(navController)
        }
        composable(
            route = MovieScreens.DetailsScreen.name + "/{movie}",
            arguments = listOf(navArgument("movie") { type = NavType.StringType })
        ) { backStackEntry ->
            val movie = backStackEntry.arguments?.getString("movie")
            DetailsScreen(navController = navController, movieData = movie)
        }
    }
}