package com.droidhats.campuscompass.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExplorePlaceEntity::class],version = 1, exportSchema = false)
abstract class ExplorePlaceDB: RoomDatabase() {

    abstract fun ExplorePlaceDAO(): ExplorePlaceDAO

    companion object {
        private var instance: ExplorePlaceDB? = null

        /**
         * Using Singleton in order to get our database
         * @return ExplorePlaceDB instance
         */
        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: Room.databaseBuilder(context,
                            ExplorePlaceDB ::class.java, "ExplorePlaceDB")
                            .createFromAsset("database/explore.db").allowMainThreadQueries().fallbackToDestructiveMigration()
                            .build().also { instance = it }
                }
    }
}