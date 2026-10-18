package com.example.appjuntavecinos.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

// No visto en clase (lo integramos con permiso del docente): ZXing dibuja el código QR.
fun generarQr(contenido: String, tamano: Int = 600): Bitmap {
    val opciones = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
    val matriz = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano, opciones)
    val bitmap = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.RGB_565)
    for (x in 0 until tamano) {
        for (y in 0 until tamano) {
            bitmap.setPixel(x, y, if (matriz[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}