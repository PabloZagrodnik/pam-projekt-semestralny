package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.model.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    // Flow - automatyczne odświeżanie listy po zmianach
    // Zapytanie - zwraca wszystko z tabeli "places"
    @Query("SELECT * FROM PLACES")
    fun getAllPlaces(): Flow<List<Place>>

    // Zapytanie - zwraca miejsce (place) o podanym id
    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: Int): Place?

    // Wstawianie do tabeli "places"
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    // Aktualizacja tabeli "places"
    @Update
    suspend fun updatePlace(place: Place)

    // Usuwanie z tabeli "places"
    @Delete
    suspend fun deletePlace(place: Place)
}