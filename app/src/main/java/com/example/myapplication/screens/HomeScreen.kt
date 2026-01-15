package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.widgets.PlaceRow

@OptIn(ExperimentalMaterial3Api::class) // do TopAppBar
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController, viewModel: PlaceViewModel) {
    // lista miejsc - sama się odświeża
    val placeList by viewModel.placesList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PAMgeo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_place_screen") }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (placeList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Brak miejsc. Kliknij + aby dodać!")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(placeList) { place ->
                        PlaceRow(place = place) { placeId ->
                            navController.navigate("details_screen/$placeId")
                        }
                    }
                }
            }
        }
    }
}