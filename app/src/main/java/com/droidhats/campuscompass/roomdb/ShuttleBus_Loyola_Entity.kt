package com.droidhats.campuscompass.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["SHUTTLE_DAY","SHUTTLE_TIME"])
class ShuttleBus_Loyola_Entity (
    @ColumnInfo(name ="SHUTTLE_DAY")
    var shuttle_day: String= "",


    @ColumnInfo(name ="SHUTTLE_TIME")
    var shuttle_time: String= ""

){
}
