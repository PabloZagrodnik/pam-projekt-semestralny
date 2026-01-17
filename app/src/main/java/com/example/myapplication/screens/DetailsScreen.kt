package com.example.myapplication.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.model.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(navController: NavController, viewModel: PlaceViewModel, placeId: String?) {
    val context = LocalContext.current
    var place by remember { mutableStateOf<Place?>(null) }

    // pobranie danych z bazy według ID przekazanego w nawigacji
    LaunchedEffect(placeId) {
        placeId?.toIntOrNull()?.let { id ->
            place = viewModel.getPlaceById(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(place?.title ?: "Szczegóły") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            place?.let { currentPlace ->
                // zdjęcie
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(currentPlace.imageUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // tytuł
                Text(text = currentPlace.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                // opis
                Text(text = "Opis:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(text = currentPlace.description, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(24.dp))

                // przycisk kierujący do map google'a
                Button(
                    onClick = {
                        // tworzenie uri (geo:lat,lng) z etykietą
                        val uri = Uri.parse("geo:${currentPlace.lat},${currentPlace.lng}?q=${currentPlace.lat},${currentPlace.lng}(${currentPlace.title})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        // otwieranie mapy
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // otworzenie w przeglądarce
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pokaż na mapie")
                }
            } ?: run {
                // ikona ładowania
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}