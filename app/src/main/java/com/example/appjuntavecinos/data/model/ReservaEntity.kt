package com.example.appjuntavecinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReservaEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0,val dependenciaId: Int,
                         val vecinoId: Int,val fecha: String,val horaInicio: String,
                         val horaFin: String,val estado: String,val fechaSolicitud: String,
                         val gestionadaPorId: Int? = null)