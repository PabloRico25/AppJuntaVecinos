package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity

@Dao
interface DependenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(dependencias: List<DependenciaComunitariaEntity>)

    @Query("DELETE FROM DependenciaComunitariaEntity")
    suspend fun borrarTodas()

    @Query("SELECT * FROM DependenciaComunitariaEntity")
    suspend fun obtenerTodas(): List<DependenciaComunitariaEntity>
}