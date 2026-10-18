package com.example.appjuntavecinos.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearUriCamara(context: Context): Uri {
    val carpeta = File(context.cacheDir, "camera").apply { mkdirs() }
    val foto = File.createTempFile("comprobante-", ".jpg", carpeta)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", foto)
}

fun guardarComprobante(context: Context, origen: Uri): String? {
    val carpeta = File(context.filesDir, "comprobantes").apply { mkdirs() }
    val destino = File.createTempFile("pago-", ".img", carpeta)
    return try {
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida ->
                val buffer = ByteArray(8192)
                var total = 0
                var leidos = entrada.read(buffer)
                while (leidos != -1) {
                    total += leidos
                    require(total <= 20 * 1024 * 1024) { "La imagen supera 20 MB" }
                    salida.write(buffer, 0, leidos)
                    leidos = entrada.read(buffer)
                }
            }
        } ?: error("No se pudo abrir la imagen")
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(destino.absolutePath, opciones)
        require(opciones.outWidth > 0 && opciones.outHeight > 0) { "El archivo no es una imagen" }
        Uri.fromFile(destino).toString()
    } catch (_: Exception) {
        destino.delete()
        null
    }
}

fun eliminarComprobante(context: Context, uri: String) {
    val ruta = Uri.parse(uri).path ?: return
    val archivo = File(ruta).canonicalFile
    if (archivo.parentFile == File(context.filesDir, "comprobantes").canonicalFile) archivo.delete()
}
