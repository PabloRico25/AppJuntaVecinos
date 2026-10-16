package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity

@Dao
interface DependenciaDao {
    @Insert
    suspend fun insertar(dependencia: DependenciaComunitariaEntity): Long

    @Query("SELECT * FROM DependenciaComunitariaEntity ORDER BY id")
    suspend fun obtenerTodas(): List<DependenciaComunitariaEntity>
}
