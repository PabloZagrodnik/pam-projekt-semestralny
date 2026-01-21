package com.example.myapplication.screens

import android.Manifest
import android.location.Geocoder
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddPlaceScreen(navController: NavController, viewModel: PlaceViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // stan formularza
    var title by remember { mutableStateOf("")}
    var description by remember { mutableStateOf("")}
    var address by remember { mutableStateOf("") } // adres z geocoding
    var imageUri by remember { mutableStateOf<Uri?>(null)}
    // tymczasowe uri przekazywane do aparatu
    var tempUri by remember { mutableStateOf<Uri?>(null)}
    var location by remember { mutableStateOf<Location?>(null)}
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // kamera (tworzenie pliku)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    // launcher aparatu
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        success -> if (success && tempUri != null) imageUri = tempUri
    }

    // funcja wywołująca aparat
    fun takePhoto() {
        val file = createImageFile()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider",file)
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    // sprawdzenie uprawnień
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted -> if (isGranted) takePhoto()
        else
            Toast.makeText(context, "Brak zgody na użycie aparatu", Toast.LENGTH_SHORT).show()
    }

    // geocoding
    fun getAddressFromLocation(lat: Double, lng: Double) {
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                fun buildAddress(addr: android.location.Address): String {
                    val street = addr.thoroughfare ?: ""
                    val number = addr.subThoroughfare ?: ""
                    val city = addr.locality ?: ""

                    return listOf("$street $number".trim(), city)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifEmpty { "Nieznany adres" }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val result = if (!addresses.isNullOrEmpty())
                            buildAddress(addresses[0])
                        else
                            "Nie znaleziono adresu"

                        scope.launch {
                            address = result
                            Toast.makeText(context, "Pobrano adres", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)

                    val result = if (!addresses.isNullOrEmpty())
                        buildAddress(addresses[0])
                    else
                        "Nie znaleziono adresu"

                    withContext(Dispatchers.Main) {
                        address = result
                        Toast.makeText(context, "Pobrano adres", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Błąd pobierania adresu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // lokalizacja
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocation || coarseLocation) {
            try {
                // pobieranie lokalizacji
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        location = lastLoc
                        getAddressFromLocation(lastLoc.latitude, lastLoc.longitude)
                        Toast.makeText(context, "Pobrano ostatnią lokalizację", Toast.LENGTH_SHORT).show()
                    } else {
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                            .addOnSuccessListener { currentLoc ->
                                if (currentLoc != null) {
                                    location = currentLoc
                                    getAddressFromLocation(currentLoc.latitude, currentLoc.longitude)
                                } else {
                                    Toast.makeText(context, "Włącz Mapy Google, aby odświeżyć lokalizację", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }.addOnFailureListener {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            location = loc
                            if (loc != null) getAddressFromLocation(loc.latitude, loc.longitude)
                        }
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Błąd uprawnień", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Brak uprawnień GPS", Toast.LENGTH_SHORT).show()
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

        // adres - wypełniane automatyczne ale można edytować
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Adres (z GPS)") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            placeholder = { Text("Pobierz lokalizację aby wypełnić") }
        )

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
                    viewModel.addPlace(
                        title,
                        description,
                        address, // przekazywanie adresu
                        imageUri.toString(),
                        location!!.latitude,
                        location!!.longitude
                    )
                    navController.popBackStack()
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