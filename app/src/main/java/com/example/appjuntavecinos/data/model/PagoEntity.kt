package com.example.appjuntavecinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PagoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val reservaId: Int? = null, val tipo: String,
    val categoria: String = "", val monto: Int, val fecha: String, val comprobanteUri: String? = null,
    val registradoPorId: Int
)