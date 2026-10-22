package com.example.gestionregistroyvalidaciondeventas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VentaEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ventas_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}