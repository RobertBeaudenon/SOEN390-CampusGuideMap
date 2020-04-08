package com.droidhats.campuscompass.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoritePlace(
    @PrimaryKey val placeId : String,
    @ColumnInfo(name = "name") val name : String?,
    @ColumnInfo(name = "latitude") val latitude : Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "address") val address : String?
)