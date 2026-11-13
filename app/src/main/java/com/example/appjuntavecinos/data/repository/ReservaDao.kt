package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appjuntavecinos.data.model.ReservaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Insert
    suspend fun insertar(reserva: ReservaEntity)

    // Guarda las reservas que entrega el servidor; si ya existía una con el mismo id, la reemplaza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(reservas: List<ReservaEntity>)

    @Query("DELETE FROM ReservaEntity")
    suspend fun borrarTodas()

    @Query("SELECT * FROM ReservaEntity WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ReservaEntity?

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