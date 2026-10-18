package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appjuntavecinos.data.model.*

@Database(
    entities = [UsuarioEntity::class, DependenciaComunitariaEntity::class, ReservaEntity::class, PagoEntity::class, IngresoEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun dependenciaDao(): DependenciaDao
    abstract fun reservaDao(): ReservaDao
    abstract fun pagoDao(): PagoDao
    abstract fun ingresoDao(): IngresoDao

    companion object {
        // Agrega la tabla de ingresos conservando las reservas y pagos de la versión anterior.
        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS IngresoEntity (reservaId INTEGER NOT NULL, fecha TEXT NOT NULL, hora TEXT NOT NULL, PRIMARY KEY(reservaId))")
            }
        }
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "junta.db"
            ).addMigrations(MIGRACION_2_3).build().also { instancia = it }
        }
    }
}
