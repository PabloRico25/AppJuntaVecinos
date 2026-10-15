package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appjuntavecinos.data.model.ReservaEntity

@Dao
interface ReservaDao {

    @Insert
    suspend fun insertar(reserva: ReservaEntity)

    @Query("SELECT * FROM ReservaEntity WHERE vecinoId = :vecinoId ORDER BY fecha")
    suspend fun obtenerPorVecino(vecinoId: Int): List<ReservaEntity>

    @Query("SELECT * FROM ReservaEntity WHERE dependenciaId = :dependenciaId AND fecha = :fecha AND estado = 'CONFIRMADA'")
    suspend fun obtenerConfirmadas(dependenciaId: Int, fecha: String): List<ReservaEntity>
    @Update
    suspend fun actualizar(reserva: ReservaEntity)
}
