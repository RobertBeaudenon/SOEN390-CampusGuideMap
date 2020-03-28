package com.droidhats.campuscompass.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

/**
 * This class will create the ShuttleBusLoyolaEntity table
 * @param ID
 * @param shuttle_day
 * @param shuttle_time
 */
@Entity
data class ShuttleBusLoyolaEntity (

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
)