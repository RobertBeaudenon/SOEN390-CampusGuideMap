package com.droidhats.campuscompass.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "IndoorLocation")
data class IndoorLocation(
    @PrimaryKey
    @ColumnInfo(name = "lID") val lID: Int,
    @ColumnInfo(name = "location_name") val name: String,
    @ColumnInfo(name = "floor_num") val floorNum: Int,
    @ColumnInfo(name = "location_type") val type:String
)  {
}