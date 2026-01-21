package com.example.myapplication.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PlaceDatabase
import com.example.myapplication.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// enum do sortowania
enum class SortOption {
    NEWEST, // najnowsze (po ID malejąco)
    OLDEST, // najstarsze (po ID rosnąco)
    ALPHABETICAL // alfabetycznie (A-Z)
}

class PlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PlaceDatabase.getDatabase(application).placeDao()

    // stan wyszukiwania
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // stan sortowania
    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption

    // główna lista - łączy dane z bazy, wyszukiwanie i sortowanie w jedno
    val placesList: StateFlow<List<Place>> = combine(
        dao.getAllPlaces(), // strumień z bazy
        _searchQuery, // strumień tekstu
        _sortOption // strumień sortowania
    ) { places, query, sort ->

        // filtrowanie
        val filteredList = if (query.isBlank()) {
            places
        } else {
            places.filter { place ->
                place.title.contains(query, ignoreCase = true) ||
                        place.description.contains(query, ignoreCase = true) ||
                        place.address.contains(query, ignoreCase = true)
            }
        }

        // sortowanie
        when (sort) {
            SortOption.NEWEST -> filteredList.sortedByDescending { it.id }
            SortOption.OLDEST -> filteredList.sortedBy { it.id }
            SortOption.ALPHABETICAL -> filteredList.sortedBy { it.title.lowercase() }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // funkcje do zmiany stanu przez UI
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOptionChanged(option: SortOption) {
        _sortOption.value = option
    }

    // metody bazy danych
    fun addPlace(title: String, description: String, address: String, imageUri: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            dao.insertPlace(Place(title = title, description = description, address = address, imageUri = imageUri, lat = lat, lng = lng))
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            dao.deletePlace(place)
        }
    }

    suspend fun getPlaceById(id: Int): Place? {
        return dao.getPlaceById(id)
    }

    fun updatePlace(place: Place) {
        viewModelScope.launch {
            dao.updatePlace(place)
        }
    }
}