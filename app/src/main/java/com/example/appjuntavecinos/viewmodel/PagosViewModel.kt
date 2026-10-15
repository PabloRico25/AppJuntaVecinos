package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.flow.Flow

class PagosViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    val pagos: Flow<List<PagoEntity>> = repositorio.observarPagos()
}