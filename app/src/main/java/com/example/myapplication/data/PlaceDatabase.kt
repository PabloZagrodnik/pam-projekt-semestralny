package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.model.Place

@Database(entities = [Place::class],version = 2, exportSchema = false)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        fun getDatabase(context: Context): PlaceDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDatabase::class.java,
                    "place_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it}
            }
        }
    }
}