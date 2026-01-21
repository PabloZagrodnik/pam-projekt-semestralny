package com.example.myapplication.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.model.Place
import java.io.File
import java.io.FileOutputStream

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

                // przycisk edycji
                IconButton(onClick = {
                    navController.navigate("edit_place_screen/${currentPlace.id}")
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                }

                // przycisk udostępniania
                IconButton(onClick = {
                    val shareText = """
                        Zobacz to miejsce: ${currentPlace.title}
                        Opis: ${currentPlace.description}
                        Lokalizacja: https://www.google.com/maps/search/?api=1&query=${currentPlace.lat},${currentPlace.lng}
                    """.trimIndent()

                    fun shareTextOnly() {
                        val textIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(textIntent, "Udostępnij tekst"))
                    }

                    try {
                        val contentResolver = context.contentResolver
                        // parsowanie Uri z bazy danych
                        val originalUri = Uri.parse(currentPlace.imageUri)

                        // otworzenie strumienia pliku
                        val inputStream = contentResolver.openInputStream(originalUri)

                        if (inputStream != null) {
                            // tworzenie pliku tymczasowego w cache
                            val cacheDir = File(context.cacheDir, "images")
                            cacheDir.mkdirs()

                            // plik o stałej nazwie
                            val tempFile = File(cacheDir, "share_image.jpg")

                            // kopiowanie danych do pliku tymczasowego
                            FileOutputStream(tempFile).use { output ->
                                inputStream.copyTo(output)
                            }
                            inputStream.close()

                            // generowanie uri dla pliku tymczasowego
                            val authority = "${context.packageName}.fileprovider"
                            val fileUri = FileProvider.getUriForFile(context, authority, tempFile)

                            // budowanie intentu
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_STREAM, fileUri)

                                // uprawnienia odczytu dla innych aplikacji
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                                // przypięcie uprawnień do intentu
                                clipData = android.content.ClipData.newRawUri(null, fileUri)
                            }

                            val chooser = Intent.createChooser(shareIntent, "Udostępnij wpis")

                            // przekazanie uprawnień dla choosera
                            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            context.startActivity(chooser)

                        } else {
                            // wysłanie samego tekstu jeśli nie udało się otworzyć pliku
                            Toast.makeText(context, "Zdjęcie nie zostało znalezione, wysyłam tekst.", Toast.LENGTH_SHORT).show()
                            shareTextOnly()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        // w razie błędu
                        shareTextOnly()
                    }

                }) {
                    Icon(Icons.Default.Share, contentDescription = "Udostępnij")
                }

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
                        val uri = Uri.parse("geo:${currentPlace.lat},${currentPlace.lng}?q=${currentPlace.lat},${currentPlace.lng}(${Uri.encode(currentPlace.title)})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
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