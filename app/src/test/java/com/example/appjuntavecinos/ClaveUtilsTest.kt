package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.hashClave
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClaveUtilsTest {

    @Test
    fun hash_de_1234_es_el_conocido() {
        val esperado = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
        assertEquals(esperado, hashClave("1234"))
    }

    @Test
    fun la_misma_clave_da_el_mismo_hash() {
        assertEquals(hashClave("abcd"), hashClave("abcd"))
    }

    @Test
    fun claves_distintas_dan_hashes_distintos() {
        assertNotEquals(hashClave("1234"), hashClave("1235"))
    }

    @Test
    fun el_hash_tiene_64_caracteres() {
        assertEquals(64, hashClave("cualquier clave").length)
    }

    @Test
    fun el_hash_no_es_la_clave_en_texto_plano() {
        assertNotEquals("1234", hashClave("1234"))
    }
}