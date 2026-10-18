package com.example.appjuntavecinos.data.repository

import androidx.room.*
import com.example.appjuntavecinos.data.model.PagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {
    @Insert
    suspend fun insertar(pago: PagoEntity): Long

    @Query("SELECT * FROM PagoEntity ORDER BY fecha DESC, id DESC")
    suspend fun obtenerTodos(): List<PagoEntity>

    @Query("SELECT reservaId FROM PagoEntity WHERE tipo = 'ARRIENDO' AND reservaId IS NOT NULL")
    fun observarReservasPagadas(): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM PagoEntity WHERE tipo = 'ARRIENDO' AND reservaId = :reservaId")
    suspend fun contarPagosDeArriendo(reservaId: Int): Int

    @Query("SELECT * FROM PagoEntity ORDER BY fecha DESC, id DESC")
    fun observarTodos(): Flow<List<PagoEntity>>

    @Delete
    suspend fun eliminar(pago: PagoEntity)
}
