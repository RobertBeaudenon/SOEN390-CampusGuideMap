package com.droidhats.campuscompass.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * This class will create our Database instance for the Shuttle Bus times
 * @param context
 */
@Database(entities = [ShuttleBus_Loyola_Entity::class,ShuttleBus_SGW_Entity::class],version = 3, exportSchema = false)
abstract class ShuttleBusDB : RoomDatabase() {

   //When we invoke this function it will call the DAO class
    abstract fun shuttleBusDAO(): ShuttleBus_DAO


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

