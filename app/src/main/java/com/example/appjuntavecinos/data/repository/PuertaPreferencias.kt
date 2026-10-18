package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.preferenciasPuerta by preferencesDataStore(name = "puerta")

data class ConfiguracionPuerta(
    val modoPuerta: Boolean = false, val salaId: Int = 0, val claveHash: String = "", val cargando: Boolean = true
)

class PuertaPreferencias(context: Context) {
    private val almacen = context.applicationContext.preferenciasPuerta
    private val modo = booleanPreferencesKey("modo_puerta")
    private val sala = intPreferencesKey("sala_id")
    private val clave = stringPreferencesKey("clave_hash")
    val configuracion = almacen.data.map {
        ConfiguracionPuerta(it[modo] ?: false, it[sala] ?: 0, it[clave].orEmpty(), cargando = false)
    }
    val modoPuerta = configuracion.map { it.modoPuerta }
    val salaId = configuracion.map { it.salaId }
    val claveHash = configuracion.map { it.claveHash }
    suspend fun activar(salaId: Int, claveHash: String) {
        almacen.edit { it[modo] = true; it[sala] = salaId; it[clave] = claveHash }
    }
    suspend fun desactivar() {
        almacen.edit { it[modo] = false; it.remove(sala); it.remove(clave) }
    }
}
