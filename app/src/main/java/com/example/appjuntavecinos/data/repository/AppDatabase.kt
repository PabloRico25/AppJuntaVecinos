package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.IngresoEntity
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Database(entities = [UsuarioEntity::class, DependenciaComunitariaEntity::class, ReservaEntity::class,
    PagoEntity::class, IngresoEntity::class], version = 3 )
abstract class AppDatabase:RoomDatabase(){
    abstract fun usuarioDao():UsuarioDao

    abstract fun dependenciaDao():DependenciaDao

    abstract fun reservaDao():ReservaDao

    abstract fun pagoDao():PagoDao

    abstract fun ingresoDao():IngresoDao

    companion object {
        @Volatile // hace que todos los hilos vean el valor más reciente.
        private var instancia: AppDatabase? = null

        fun obtener(context: Context):AppDatabase {
            return instancia ?:synchronized(this) {
                val nueva=Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "junta.db"
                ).build()
                instancia = nueva
                nueva
            }
        }
    }
}