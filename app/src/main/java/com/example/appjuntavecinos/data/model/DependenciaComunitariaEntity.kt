package com.example.appjuntavecinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DependenciaComunitariaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)
