package com.example.appjuntavecinos.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

fun generarQr(contenido: String, tamano: Int = 600): Bitmap {
    val matriz = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano,
        mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 4))
    val pixeles = IntArray(tamano * tamano) { i ->
        if (matriz[i % tamano, i / tamano]) Color.BLACK else Color.WHITE
    }
    return Bitmap.createBitmap(pixeles, tamano, tamano, Bitmap.Config.ARGB_8888)
}
