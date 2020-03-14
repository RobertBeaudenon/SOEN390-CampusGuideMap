package com.droidhats.campuscompass.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ShuttleBus_Loyola_Entity::class,ShuttleBus_SGW_Entity::class],version = 3, exportSchema = false)
abstract class AppDB : RoomDatabase() {

   //When we invoke this function it will call the DAO class
    abstract fun shuttleBusDAO(): ShuttleBus_DAO

    companion object {
        private var INSTANCE: AppDB? = null
        fun getInstance(context: Context): AppDB? {
            if (INSTANCE == null) {
                synchronized(AppDB::class) {
                    INSTANCE = Room.databaseBuilder(context,
                        AppDB::class.java, "CampusCompassDB")
                        .createFromAsset("database/ShuttleBus.db").fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }


}