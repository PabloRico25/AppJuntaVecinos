package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.data.model.PagoEntity


@Dao
interface PagoDao {

    @Insert
    suspend fun insertar(pago: PagoEntity)

    @Query("SELECT * FROM PagoEntity ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<PagoEntity>

    @Delete
    suspend fun eliminar(pago: PagoEntity)
}