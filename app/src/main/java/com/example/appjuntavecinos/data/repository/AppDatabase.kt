package com.example.appjuntavecinos.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Database(entities = [UsuarioEntity::class, DependenciaComunitariaEntity::class], version = 1 )
abstract class AppDatabase : RoomDatabase(){
    abstract fun usuarioDao(): UsuarioDao

    abstract fun dependenciaDao(): DependenciaDao
}