package il.co.erg.mykumve.data.model

import il.co.erg.mykumve.util.Converters
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import il.co.erg.mykumve.data.data_classes.Equipment
import il.co.erg.mykumve.util.ShareLevel

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripInfo::class,
            parentColumns = ["id"],
            childColumns = ["trip_info_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"]), Index(value = ["trip_info_id"])]
)
@TypeConverters(Converters::class)
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "gather_time") var gatherTime: Long?,
    @ColumnInfo(name = "participants") var participants: MutableList<User>?,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "equipment") var equipment: MutableList<Equipment>?,
    @ColumnInfo(name = "user_id") var userId: Long,
    @ColumnInfo(name = "trip_info_id") var tripInfoId: Long?,
    @ColumnInfo(name = "notes") var notes: MutableList<String>?,
    @ColumnInfo(name = "end_date") var endDate: Long?,
    @ColumnInfo(name = "invitations") var invitations: MutableList<TripInvitation> = mutableListOf(),
    @ColumnInfo(name = "share_level") var shareLevel: ShareLevel = ShareLevel.PUBLIC
)