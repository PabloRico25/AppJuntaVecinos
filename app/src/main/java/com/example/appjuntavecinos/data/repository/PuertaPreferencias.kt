package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes_puerta")

class PuertaPreferencias(context: Context) {

    private val almacen = context.applicationContext.dataStore

    private val modoPuertaKey = booleanPreferencesKey("modo_puerta")
    private val salaIdKey = intPreferencesKey("sala_id")
    private val claveHashKey = stringPreferencesKey("clave_hash")
    private val tokenKey = stringPreferencesKey("token_puerta")

    val modoPuerta: Flow<Boolean> = almacen.data.map { it[modoPuertaKey] ?: false }
    val salaId: Flow<Int> = almacen.data.map { it[salaIdKey] ?: 0 }
    val claveHash: Flow<String> = almacen.data.map { it[claveHashKey] ?: "" }

    // El token de 7 días que el servidor entrega al activar la puerta; vacío si la puerta no está activa
    val token: Flow<String> = almacen.data.map { it[tokenKey] ?: "" }

    suspend fun activar(salaId: Int, claveHash: String, token: String) {
        almacen.edit { preferencias ->
            preferencias[modoPuertaKey] = true
            preferencias[salaIdKey] = salaId
            preferencias[claveHashKey] = claveHash
            preferencias[tokenKey] = token
        }
    }

    suspend fun desactivar() {
        almacen.edit { preferencias ->
            preferencias[modoPuertaKey] = false
            preferencias.remove(salaIdKey)
            preferencias.remove(claveHashKey)
            preferencias.remove(tokenKey)
        }
    }
}