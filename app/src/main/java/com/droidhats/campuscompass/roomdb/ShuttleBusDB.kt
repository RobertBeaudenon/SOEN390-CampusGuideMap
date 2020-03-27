package com.droidhats.campuscompass.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * This class will create our Database instance for the Shuttle Bus times
 */
@Database(entities = [ShuttleBusLoyolaEntity::class,ShuttleBusSGWEntity::class],version = 3, exportSchema = false)
abstract class ShuttleBusDB : RoomDatabase() {

   //When we invoke this function it will call the DAO class
    abstract fun shuttleBusDAO(): ShuttleBusDAO


    companion object {
        private var instance: ShuttleBusDB? = null

        /**
         * Using Singleton in order to get our database
         * @return ShuttleBusDB instance
         */
        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: Room.databaseBuilder(context,
                            ShuttleBusDB ::class.java, "CampusCompassDB")
                        .createFromAsset("database/ShuttleBus.db").allowMainThreadQueries().fallbackToDestructiveMigration()
                        .build().also { instance = it }
                }
    }
}

