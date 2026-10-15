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
    PagoEntity::class], version = 2 )
abstract class AppDatabase : RoomDatabase(){
    abstract fun usuarioDao(): UsuarioDao

    abstract fun dependenciaDao(): DependenciaDao

    abstract fun reservaDao(): ReservaDao

    abstract fun pagoDao(): PagoDao

    // companion object: lo que va aquí pertenece a la clase, no a cada objeto.
    // Se usa sin crear un AppDatabase: AppDatabase.obtener(context).
    // Abre la base una sola vez para toda la app (singleton).
    // databaseBuilder sale de la documentación oficial de Room (link de la presentación 2.5.1).
    // synchronized evita que dos partes de la app la creen al mismo tiempo.
    // @Volatile: todos los hilos ven el valor actual de la variable.
    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val nueva = Room.databaseBuilder(
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