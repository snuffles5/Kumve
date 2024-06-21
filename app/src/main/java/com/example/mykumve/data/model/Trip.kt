package com.example.mykumve.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mykumve.util.Converters

@Entity(tableName = "trips",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(Converters::class)

data class Trip(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "notes")
    val notes: String?,

//    @ColumnInfo(name = "place")
//    val place: String?,

//    @ColumnInfo(name = "difficulty")
//    val level: DifficultyLevel,

    @ColumnInfo(name = "start_date")
    val startDate: Long?,

    @ColumnInfo(name = "end_date")
    val endDate: Long?,

    @ColumnInfo(name = "photo_uri")
    val photo: String?,

    @ColumnInfo(name = "user_id")
    var userId: Int,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
