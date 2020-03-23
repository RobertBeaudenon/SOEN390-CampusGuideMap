package com.droidhats.campuscompass

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.repositories.IndoorLocationDao

/**
 * The Room database for this app
 * Entities: Indoor Locations
 */

@Database(entities = [IndoorLocation::class], version = 1, exportSchema = true)
abstract class IndoorLocationDatabase : RoomDatabase() {
    abstract fun indoorLocationDao(): IndoorLocationDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: IndoorLocationDatabase? = null

        fun getInstance(context: Context): IndoorLocationDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database
        private fun buildDatabase(context: Context): IndoorLocationDatabase {
            return Room.databaseBuilder(context, IndoorLocationDatabase::class.java, "IndoorLocationSchema.db")
                .createFromAsset("database/IndoorLocationSchema.db")
                .build()
        }
    }
}