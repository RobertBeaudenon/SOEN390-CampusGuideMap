package com.droidhats.campuscompass.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

/**
 * This class will create the ShuttleBus_Loyola_Entity table
 * @param ID
 * @param SHUTTLE_DAY
 * @param SHUTTLE_TIME
 */
@Entity()
data class ShuttleBus_Loyola_Entity (

    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "ID")
    var ID: Int,

    @NotNull
    @ColumnInfo(name ="SHUTTLE_DAY")
    var shuttle_day: String= "",

    @NotNull
    @ColumnInfo(name ="SHUTTLE_TIME")
    var shuttle_time: String= ""
){}