@file:JvmName("PlaceNavigationKt")

package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.screens.AddPlaceScreen
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.PlaceViewModel

@Composable
fun PlaceNavigation() {
    val navController = rememberNavController()
    // tworzenie viewmodelu (jeden wspólny dla całości)
    val placeViewModel: PlaceViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home_screen") {

        // ekran główny (lista)
        composable("home_screen") {
            HomeScreen(navController = navController, viewModel = placeViewModel)
        }

        // ekran dodawania
        composable("add_place_screen") {
            AddPlaceScreen(navController = navController, viewModel = placeViewModel)
        }

        // szczegóły
        composable("details_screen/{placeId}") {
            // TODO
        }
    }
}