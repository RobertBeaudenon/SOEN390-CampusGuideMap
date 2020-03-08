package com.droidhats.campuscompass.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ShuttleBus_Entity {
    //columns names
    @PrimaryKey
    var emp_id: Int = 0

    @ColumnInfo(name ="EMP_NAME")
    var emp_name: String= ""

    @ColumnInfo(name ="EMP_POST")
    var emp_post: String= ""
}