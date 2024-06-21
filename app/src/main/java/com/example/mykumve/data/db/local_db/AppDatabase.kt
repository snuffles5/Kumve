package com.example.mykumve.data.db.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.User
import com.example.mykumve.util.Converters

/**
 * Abstract class for Room database setup.
 * Includes DAOs for accessing the User and Trip tables.
 *
 * TODO: Add any additional entities and their DAOs if necessary.
 */
@Database(entities = [User::class, Trip::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun tripInfoDao(): TripInfoDao
    abstract fun userDao(): UserDao

    companion object{
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context) = instance ?: synchronized(this){
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                "kumve_db")
                .allowMainThreadQueries().build()
        }
    }
}
//