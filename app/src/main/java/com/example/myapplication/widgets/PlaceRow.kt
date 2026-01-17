package com.example.myapplication.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.model.Place

@Composable
fun PlaceRow(place: Place, onItemClick: (String) -> Unit = {}) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onItemClick(place.id.toString()) },
        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // obrazek
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                shape = RoundedCornerShape(corner = CornerSize(8.dp)),
                tonalElevation = 4.dp
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = place.imageUri),
                    contentDescription = "Zdjęcie miejsca",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // tekst
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = place.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "GPS: ${place.lat}, ${place.lng}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}