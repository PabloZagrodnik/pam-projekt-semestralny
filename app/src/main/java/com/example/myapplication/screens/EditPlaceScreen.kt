package com.example.myapplication.screens

import android.Manifest
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.model.Place
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditPlaceScreen(navController: NavController, viewModel: PlaceViewModel, placeId: String?) {
    val context = LocalContext.current

    // stany formularza
    var existingPlace by remember { mutableStateOf<Place?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // ładowanie danych istniejącego miejsca
    LaunchedEffect(placeId) {
        placeId?.toIntOrNull()?.let { id ->
            val place = viewModel.getPlaceById(id)
            if (place != null) {
                existingPlace = place
                title = place.title
                description = place.description
                imageUri = Uri.parse(place.imageUri)
            }
        }
    }

    // z AddPlaceScreen
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) imageUri = tempUri
    }

    fun takePhoto() {
        val file = createImageFile()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) takePhoto() else Toast.makeText(context, "Brak zgody na aparat", Toast.LENGTH_SHORT).show()
    }

    // interfejs użytkownika
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edytuj Miejsce", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nazwa miejsca") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Opis") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
            Text("Zmień Zdjęcie")
        }

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Przycisk zapisu
        Button(
            onClick = {
                if (title.isNotEmpty() && existingPlace != null) {
                    // tworzenie obiektu Place z zachowaniem starego id i lokalizacji
                    val updatedPlace = existingPlace!!.copy(
                        title = title,
                        description = description,
                        imageUri = imageUri.toString()
                    )

                    viewModel.updatePlace(updatedPlace)
                    navController.popBackStack() // powrót
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zapisz Zmiany")
        }
    }
}