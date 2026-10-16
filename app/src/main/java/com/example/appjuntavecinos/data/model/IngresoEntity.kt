package com.example.appjuntavecinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IngresoEntity(
    @PrimaryKey val reservaId: Int,
    val fecha: String,
    val hora: String
)