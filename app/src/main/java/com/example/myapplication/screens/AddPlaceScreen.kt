package com.example.myapplication.screens

import android.Manifest
import android.location.Location
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddPlaceScreen(navController: NavController, viewModel: PlaceViewModel) {
    val context = LocalContext.current

    // stan formularza
    var title by remember { mutableStateOf("")}
    var description by remember { mutableStateOf("")}
    var imageUri by remember { mutableStateOf<Uri?>(null)}
    // tymczasowe uri przekazywane do aparatu
    var tempUri by remember { mutableStateOf<Uri?>(null)}
    var location by remember { mutableStateOf<Location?>(null)}

    // kamera (tworzenie pliku)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG${timeStamp}_", ".jpg", storageDir)
    }

    // launcher aparatu
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        success -> if (success && tempUri != null) imageUri = tempUri
    }

    // funcja wywołująca aparat
    fun takePhoto() {
        val file = createImageFile()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider",file)
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    // sprawdzenie uprawnień
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted -> if (isGranted) takePhoto()
        else
            Toast.makeText(context, "Brak zgody na użycie aparatu", Toast.LENGTH_SHORT).show()
    }

    // logika lokalizacji
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {

            try {
                // pobranie aktualnej lokalizacji zamiast ostatniej
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            location = loc
                        } else {
                            Toast.makeText(context, "Brak lokalizacji", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Błąd pobierania lokalizacji", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: SecurityException) {
                // niemożliwe do osiągnięcia
            }
        }
    }

    // ui ekranu
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // przewijanie ekranu
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nowe Miejsce", style = MaterialTheme.typography.headlineMedium)

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

        // zdjęcia
        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
            Text("Zrób Zdjęcie")
        }

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }

        // GPS
        Button(onClick = {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }) {
            Text(if (location == null) "Pobierz Lokalizację" else "GPS OK: ${location!!.latitude}, ${location!!.longitude}")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // button zapisz
        Button(
            onClick = {
                if (title.isNotEmpty() && imageUri != null && location != null) {
                    viewModel.addPlace(title, description, imageUri.toString(), location!!.latitude, location!!.longitude)
                    navController.popBackStack() // Wraca do listy po zapisaniu
                } else {
                    Toast.makeText(context, "Uzupełnij nazwę, zdjęcie i GPS!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotEmpty()
        ) {
            Text("ZAPISZ")
        }
    }

}
