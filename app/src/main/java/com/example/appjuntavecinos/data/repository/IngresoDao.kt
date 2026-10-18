package com.example.appjuntavecinos.data.repository

import androidx.room.*
import com.example.appjuntavecinos.data.model.IngresoEntity

@Dao
interface IngresoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(ingreso: IngresoEntity): Long

    @Query("SELECT reservaId FROM IngresoEntity")
    suspend fun obtenerReservasUsadas(): List<Int>
}
