package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appjuntavecinos.data.model.*

@Database(
    entities = [UsuarioEntity::class, DependenciaComunitariaEntity::class, ReservaEntity::class, PagoEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun dependenciaDao(): DependenciaDao
    abstract fun reservaDao(): ReservaDao
    abstract fun pagoDao(): PagoDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "junta.db"
            ).build().also { instancia = it }
        }
    }
}
