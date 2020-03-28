package com.droidhats.campuscompass.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

/**
 * This class will create the ShuttleBusSGWEntity table
 * @param ID
 * @param shuttle_day
 * @param shuttle_time
 */
@Entity
data class ShuttleBusSGWEntity(

    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "ID")
    var ID: Int,

    @ColumnInfo(name ="SHUTTLE_DAY")
    @NotNull
    var shuttle_day: String= "",

    @ColumnInfo(name ="SHUTTLE_TIME")
    @NotNull
    var shuttle_time: String= ""
)