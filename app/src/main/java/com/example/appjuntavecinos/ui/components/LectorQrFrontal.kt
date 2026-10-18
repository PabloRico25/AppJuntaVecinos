package com.example.appjuntavecinos.ui.components

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun LectorQrFrontal(modifier: Modifier = Modifier, onCodigo: (String?) -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    val cicloDeVida = LocalLifecycleOwner.current
    val codigoActual by rememberUpdatedState(onCodigo)
    val errorActual by rememberUpdatedState(onError)
    val vista = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
    AndroidView(factory = { vista }, modifier = modifier.clipToBounds())
    DisposableEffect(cicloDeVida, vista) {
        val futuro = ProcessCameraProvider.getInstance(context)
        val executor = Executors.newSingleThreadExecutor()
        val principal = ContextCompat.getMainExecutor(context)
        val lector = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(vista.surfaceProvider) }
        val analisis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        var proveedor: ProcessCameraProvider? = null
        var cerrado = false
        analisis.setAnalyzer(executor) { frame ->
            val imagen = frame.image
            if (imagen == null || cerrado) frame.close()
            else {
                try {
                    lector.process(InputImage.fromMediaImage(imagen, frame.imageInfo.rotationDegrees))
                        .addOnSuccessListener(principal) { codigos ->
                            if (!cerrado) codigoActual(codigos.firstOrNull()?.rawValue)
                        }
                        .addOnFailureListener(principal) { if (!cerrado) errorActual("No se pudo analizar la imagen de la cámara") }
                        .addOnCompleteListener { frame.close() }
                } catch (_: Exception) { frame.close() }
            }
        }
        futuro.addListener({
            if (!cerrado) {
                try {
                    proveedor = futuro.get()
                    if (proveedor!!.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA))
                        proveedor!!.bindToLifecycle(cicloDeVida, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analisis)
                    else errorActual("Este aparato no tiene cámara frontal disponible")
                } catch (_: Exception) { errorActual("No se pudo iniciar la cámara frontal. Revisa su permiso.") }
            }
        }, principal)
        onDispose {
            cerrado = true
            analisis.clearAnalyzer()
            proveedor?.unbind(preview, analisis)
            lector.close()
            executor.shutdown()
        }
    }
}
