package com.example.appjuntavecinos.data.model

import com.example.appjuntavecinos.utils.DatosQr
import com.example.appjuntavecinos.utils.EstadoIngreso

data class EscanearUiState(val codigo:String="",val resultado:ResultadoEscaneo?=null,
                           val error:String?=null)

data class ResultadoEscaneo(val estado:EstadoIngreso,val datos:DatosQr?,val motivo:String?=null)