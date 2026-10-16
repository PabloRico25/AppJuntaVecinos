package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.ResumenFinanciero
import com.example.appjuntavecinos.utils.calcularResumen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ResumenViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val hoy = Calendar.getInstance().time
    private val mes = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(hoy)

    val nombreMes: String = SimpleDateFormat("MMMM yyyy", Locale("es", "CL"))
        .format(hoy)
        .replaceFirstChar { it.uppercase() }

    val resumen: Flow<ResumenFinanciero> = repositorio.observarPagos()
        .map { pagos -> calcularResumen(pagos, mes) }
}