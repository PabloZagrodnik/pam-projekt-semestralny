package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.widgets.PlaceRow

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController, viewModel: PlaceViewModel) {
    // lista miejsc - sama się odświeża
    val placeList by viewModel.placesList.collectAsState(initial = emptyList())

    // szkielet ekranu
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PAMgeo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            // + do ekranu dodawania
            FloatingActionButton(onClick = { navController.navigate("add_place_screen") }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // obsługa pustej listy
            if (placeList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Brak miejsc. Kliknij + aby dodać!")
                }
            } else {
                // lista
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = placeList, key = { place -> place.id }) { place ->

                        // inicjalizacja stanu przesuwania
                        val dismissState = rememberSwipeToDismissBoxState()

                        // reakcja na przesunięcie
                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deletePlace(place)
                            }
                        }

                        // obsługa gestu przesuwania
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false, // blokowanie przesuwania w prawo
                            enableDismissFromEndToStart = true, // w lewo usuwanie
                            // tło z koszem TODO do usprawnienia
                            backgroundContent = {
                                // gradient
                                val alpha = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                    dismissState.progress.coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                                val dynamicColor = Color.Red.copy(alpha = alpha)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(dynamicColor)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd // ikona po prawej
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Usuń",
                                        tint = Color.White.copy(alpha = alpha)
                                    )
                                }
                            },
                            // zawartość wiersza
                            content = {
                                PlaceRow(place = place) { placeId ->
                                    navController.navigate("details_screen/$placeId")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}