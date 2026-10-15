package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appjuntavecinos.data.model.ReservaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Insert
    suspend fun insertar(reserva: ReservaEntity)

    @Query("SELECT * FROM ReservaEntity WHERE vecinoId = :vecinoId ORDER BY fecha")
    suspend fun obtenerPorVecino(vecinoId: Int): List<ReservaEntity>

    @Query("SELECT * FROM ReservaEntity WHERE dependenciaId = :dependenciaId AND fecha = :fecha AND estado = 'CONFIRMADA'")
    suspend fun obtenerConfirmadas(dependenciaId: Int, fecha: String): List<ReservaEntity>

    @Query("SELECT * FROM ReservaEntity WHERE vecinoId = :vecinoId ORDER BY fecha, horaInicio")
    fun observarPorVecino(vecinoId: Int): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM ReservaEntity ORDER BY fecha, horaInicio")
    fun observarTodas(): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM ReservaEntity WHERE estado = 'CONFIRMADA' AND id NOT IN (SELECT reservaId FROM PagoEntity WHERE tipo = 'ARRIENDO' AND reservaId IS NOT NULL) ORDER BY fecha, horaInicio")
    suspend fun obtenerConfirmadasSinPago(): List<ReservaEntity>

    @Update
    suspend fun actualizar(reserva: ReservaEntity)
}