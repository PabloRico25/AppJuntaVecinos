package com.example.appjuntavecinos.utils

import java.security.MessageDigest

fun hashClave(clave: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(clave.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
