package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.data.model.PagoEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface PagoDao {

    @Insert
    suspend fun insertar(pago: PagoEntity)

    @Query("SELECT * FROM PagoEntity ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<PagoEntity>

    @Query("SELECT * FROM PagoEntity ORDER BY fecha DESC, id DESC")
    fun observarTodos(): Flow<List<PagoEntity>>

    @Delete
    suspend fun eliminar(pago: PagoEntity)
}