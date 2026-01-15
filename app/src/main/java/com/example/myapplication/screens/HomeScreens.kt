package com.example.myapplication.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.myapplication.model.getMovies
import com.example.myapplication.navigation.MovieScreens
import com.example.myapplication.widgets.MovieRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreens(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Movies") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Magenta
                )
            )
        }
    ) { paddingValues ->
        MainContent(navController = navController, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun MainContent(navController: NavController, modifier: Modifier = Modifier) {
    val movieList = getMovies()

    Column(modifier = modifier) {
        LazyColumn {
            items(items = movieList) { movie ->
                MovieRow(movie = movie) { id ->
                    navController.navigate(route = MovieScreens.DetailsScreen.name + "/$id")
                }
            }
        }
    }
}