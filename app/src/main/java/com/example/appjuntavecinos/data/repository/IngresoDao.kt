package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appjuntavecinos.data.model.IngresoEntity

@Dao
interface IngresoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(ingreso: IngresoEntity)

    @Query("SELECT reservaId FROM IngresoEntity")
    suspend fun obtenerReservasUsadas(): List<Int>
}