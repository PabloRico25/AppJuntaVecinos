package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity



@Dao
interface DependenciaDao {
    @Insert
    suspend fun insertar(dependencia: DependenciaComunitariaEntity)

    @Query("SELECT * FROM DependenciaComunitariaEntity")
    suspend fun obtenerTodas(): List<DependenciaComunitariaEntity>
}