package com.droidhats.campuscompass.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

/**
 * This class will create the ExplorePlaceEntity table
 * @param name
 * @param address
 * @param rating
 * @param image
 * @param category
 * @param food_type
 * @param placeID
 */
@Entity(tableName = "ExplorePlaceEntity")
class ExplorePlaceEntity (

    @NotNull
    @ColumnInfo(name = "name")
    var name: String = "",

    @NotNull
    @ColumnInfo(name ="address")
    var address: String= "",

    @NotNull
    @ColumnInfo(name ="rating")
    var rating: Int = 0,

    @NotNull
    @ColumnInfo(name ="category")
    var category: String = "",

    @NotNull
    @ColumnInfo(name ="food_type")
    var food_type: String = "",

    @NotNull
    @PrimaryKey
    @ColumnInfo(name ="placeID")
    var placeID: String = "",

    @NotNull
    @ColumnInfo(name ="distance")
    var distance: Int


)