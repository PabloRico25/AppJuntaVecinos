package com.example.appjuntavecinos.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appjuntavecinos.data.model.UsuarioEntity

@Database(entities = [UsuarioEntity::class], version = 1 )
abstract class AppDatabase : RoomDatabase(){
    abstract fun usuarioDao(): UsuarioDao
}