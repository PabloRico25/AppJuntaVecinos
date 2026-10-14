package com.example.appjuntavecinos.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM UsuarioEntity WHERE correo = :correo")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Insert
    suspend fun insertar(usuario: UsuarioEntity)
}