package com.example.appjuntavecinos.utils

import java.security.MessageDigest

// Convierte la clave en un hash SHA-256, que no se puede deshacer.
// Usa MessageDigest, una clase de Java que ya viene en Android (documentación oficial).
// Los bytes se pasan a hexadecimal para guardarlos como texto.

fun hashClave(clave: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(clave.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}