package com.example.casinoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.casinoapp.data.dao.UserDao
import com.example.casinoapp.data.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false) // <- quita warning
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "casino_db"
                ).build().also { INSTANCE = it }
            }
    }
}