package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.core.LogEntity
import com.example.core.LogDao

@Database(entities = [LogEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}