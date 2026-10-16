package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM UsuarioEntity WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM UsuarioEntity WHERE id = :id")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    @Query("SELECT * FROM UsuarioEntity ORDER BY nombre")
    suspend fun obtenerTodos(): List<UsuarioEntity>

    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long
}
