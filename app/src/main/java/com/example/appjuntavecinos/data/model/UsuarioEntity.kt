package com.example.appjuntavecinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UsuarioEntity(@PrimaryKey(autoGenerate = true) val id: Int=0,val nombre:String,
                         val correo:String,val clave:String,val rol:String,val cargo:String="")