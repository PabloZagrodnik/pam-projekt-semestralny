package com.example.myapplication.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PlaceDatabase
import com.example.myapplication.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {

    // inicjalizacja bazy danych
    private val db = PlaceDatabase.getDatabase(application)
    private val dao = db.placeDao()

    // lista miejsc, którą będzie obserwował UI
    val placesList: Flow<List<Place>> = dao.getAllPlaces()

    // funkcja dodająca wpis (w tle)
    fun addPlace(title: String, description: String, imageUri: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val place = Place(
                title = title,
                description = description,
                imageUri = imageUri,
                lat = lat,
                lng = lng
            )
            dao.insertPlace(place)
        }
    }

    // funkcja do pobierania szczegółów konkretnego miejsca:
    suspend fun getPlaceById(id: Int): Place? {
        return dao.getPlaceById(id)
    }

    // funkcja do usuwania
    fun deletePlace(place: Place) {
        viewModelScope.launch {
            dao.deletePlace(place)
        }
    }
}