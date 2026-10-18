package com.example.appjuntavecinos.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearUriCamara(context: Context): Uri {
    val carpeta = File(context.cacheDir, "camera")
    carpeta.mkdirs()
    val archivo = File.createTempFile("comprobante_", ".jpg", carpeta)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

// No visto en clase (lo integramos con permiso del docente): la foto se copia a filesDir para que no se pierda.
fun guardarComprobante(context: Context, origen: Uri): String? {
    val carpeta = File(context.filesDir, "comprobantes")
    carpeta.mkdirs()
    val destino = File(carpeta, "comprobante_${System.currentTimeMillis()}.jpg")
    return try {
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida ->
                entrada.copyTo(salida)
            }
        }
        Uri.fromFile(destino).toString()
    } catch (e: Exception) {
        null
    }
}