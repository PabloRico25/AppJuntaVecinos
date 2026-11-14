package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)

    @Query("DELETE FROM UsuarioEntity")
    suspend fun borrarTodos()

    @Query("SELECT * FROM UsuarioEntity")
    suspend fun obtenerTodos(): List<UsuarioEntity>
}