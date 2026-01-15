package com.example.myapplication.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.myapplication.model.Movie


@Composable
fun MovieRow(
    movie: Movie,
    onItemClick: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable {
                onItemClick(movie.id.toString())
            },
        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.padding(12.dp).size(100.dp),
                shape = RectangleShape,
                tonalElevation = 4.dp
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.images[0])
                        .crossfade(true)
                        .transformations(CircleCropTransformation())
                        .build()
                )
                Image(painter = painter, contentDescription = "Movie Poster")
            }

            Column(modifier = Modifier.padding(4.dp)) {
                Text(text = movie.title, style = MaterialTheme.typography.titleLarge)
                Text(text = "Director: ${movie.director}", style = MaterialTheme.typography.bodyMedium)

                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide Details" else "Show Details")
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Text(text = "Plot: ${movie.plot}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Actors: ${movie.actors}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}