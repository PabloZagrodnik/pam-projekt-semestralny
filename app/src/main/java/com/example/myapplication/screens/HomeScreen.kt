package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.widgets.PlaceRow
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController, viewModel: PlaceViewModel) {
    // obserwowanie stanów z ViewModelu
    val placeList by viewModel.placesList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    // stan dla menu sortowania
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // search bar
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)
            ) {
                // wiersz zawierający pole wyszukiwania i przycisk sortowania
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // pole wyszukiwania
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Szukaj") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f), // zajmuje dostępną przestrzeń
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp), // zaokrąglony kształt
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // przycisk Sortowania z Menu
                    Box {
                        IconButton(
                            onClick = { isSortMenuExpanded = true },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Sortuj")
                        }

                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Najnowsze") },
                                onClick = {
                                    viewModel.onSortOptionChanged(SortOption.NEWEST)
                                    isSortMenuExpanded = false
                                },
                                leadingIcon = {
                                    // zaznaczenie aktualnej opcji
                                    if (sortOption == SortOption.NEWEST) {
                                        Text("✓", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Najstarsze") },
                                onClick = {
                                    viewModel.onSortOptionChanged(SortOption.OLDEST)
                                    isSortMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (sortOption == SortOption.OLDEST) {
                                        Text("✓", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alfabetycznie (A-Z)") },
                                onClick = {
                                    viewModel.onSortOptionChanged(SortOption.ALPHABETICAL)
                                    isSortMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (sortOption == SortOption.ALPHABETICAL) {
                                        Text("✓", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            // + do ekranu dodawania
            FloatingActionButton(onClick = { navController.navigate("add_place_screen") }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (placeList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (searchQuery.isNotEmpty()) {
                        Text("Nie znaleziono miejsc pasujących do zapytania.")
                    } else {
                        Text("Brak miejsc. Kliknij + aby dodać")
                    }
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
                            backgroundContent = {
                                // pobranie aktualnego przesunięcia
                                val offset = try { dismissState.requireOffset() } catch (e: Exception) { 0f }
                                
                                // obliczenie alpha na podstawie przesunięcia
                                val alpha = if (offset < 0) {
                                    (abs(offset) / 400f).coerceIn(0f, 1f)
                                } else 0f

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, Color.Red.copy(alpha = alpha * 0.7f)),
                                                startX = 300f 
                                            ),
                                            shape = RoundedCornerShape(16.dp) // okrągłe rogi gradientu
                                        )
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
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