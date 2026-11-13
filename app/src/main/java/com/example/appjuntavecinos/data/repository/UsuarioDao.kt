package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM UsuarioEntity WHERE correo = :correo")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)

    @Query("DELETE FROM UsuarioEntity")
    suspend fun borrarTodos()

    @Query("SELECT * FROM UsuarioEntity")
    suspend fun obtenerTodos(): List<UsuarioEntity>

    @Insert
    suspend fun insertar(usuario: UsuarioEntity)
}