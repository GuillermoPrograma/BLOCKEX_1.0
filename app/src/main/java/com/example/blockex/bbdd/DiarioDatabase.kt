package com.example.blockex.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DiarioEntry::class], version = 1)
abstract class DiarioDatabase : RoomDatabase() {

    abstract fun diarioDao(): DiarioDao

    companion object {
        @Volatile
        private var INSTANCE: DiarioDatabase? = null

        fun getDatabase(context: Context): DiarioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiarioDatabase::class.java,
                    "diario_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}