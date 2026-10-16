package com.example.appjuntavecinos.data.repository

import androidx.room.*
import com.example.appjuntavecinos.data.model.ReservaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {
    @Insert
    suspend fun insertar(reserva: ReservaEntity): Long

    @Query("SELECT * FROM ReservaEntity WHERE vecinoId = :vecinoId ORDER BY fecha DESC, horaInicio")
    suspend fun obtenerPorVecino(vecinoId: Int): List<ReservaEntity>

    @Query("SELECT * FROM ReservaEntity WHERE vecinoId = :vecinoId ORDER BY fecha DESC, horaInicio")
    fun observarPorVecino(vecinoId: Int): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM ReservaEntity ORDER BY CASE WHEN estado = 'PENDIENTE' THEN 0 ELSE 1 END, fecha, horaInicio")
    fun observarTodas(): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM ReservaEntity WHERE dependenciaId = :dependenciaId AND fecha = :fecha AND estado = 'CONFIRMADA'")
    suspend fun obtenerConfirmadas(dependenciaId: Int, fecha: String): List<ReservaEntity>

    @Query("SELECT * FROM ReservaEntity WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ReservaEntity?

    @Query("SELECT * FROM ReservaEntity WHERE estado = 'CONFIRMADA' AND id NOT IN (SELECT reservaId FROM PagoEntity WHERE tipo = 'ARRIENDO' AND reservaId IS NOT NULL)")
    suspend fun obtenerConfirmadasSinPago(): List<ReservaEntity>

    @Update
    suspend fun actualizar(reserva: ReservaEntity)
}
