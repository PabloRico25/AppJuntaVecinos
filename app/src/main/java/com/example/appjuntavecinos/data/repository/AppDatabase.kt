package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Database(entities = [UsuarioEntity::class, DependenciaComunitariaEntity::class, ReservaEntity::class,
    PagoEntity::class], version = 4 )
abstract class AppDatabase:RoomDatabase(){
    abstract fun usuarioDao():UsuarioDao

    abstract fun dependenciaDao():DependenciaDao

    abstract fun reservaDao():ReservaDao

    abstract fun pagoDao():PagoDao

    companion object {
        @Volatile // hace que todos los hilos vean el valor más reciente.
        private var instancia: AppDatabase? = null

        fun obtener(context: Context):AppDatabase {
            return instancia ?:synchronized(this) {
                val nueva=Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "junta.db"
                )
                    // Room es solo una copia de lo que tiene el servidor: si cambia el esquema,
                    // se borra y se vuelve a llenar en vez de migrar.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                instancia = nueva
                nueva
            }
        }
    }
}