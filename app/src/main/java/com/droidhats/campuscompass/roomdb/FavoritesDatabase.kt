package com.droidhats.campuscompass.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.repositories.FavoritePlacesDao

@Database(entities = [FavoritePlace::class], version = 3, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoritePlacesDao(): FavoritePlacesDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: FavoritesDatabase? = null

        fun getInstance(context: Context): FavoritesDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database
        private fun buildDatabase(context: Context): FavoritesDatabase {
            return Room.databaseBuilder(context, FavoritesDatabase::class.java, "favorites-db.rdb")
                .allowMainThreadQueries().fallbackToDestructiveMigration()
                .build()
        }
    }
}