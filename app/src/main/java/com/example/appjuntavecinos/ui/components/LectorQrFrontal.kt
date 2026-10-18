package com.example.appjuntavecinos.ui.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// No visto en clase (lo integramos con permiso del docente): CameraX y ML Kit leen el QR con la cámara frontal.
// Lector de QR propio con la cámara FRONTAL.
// Por qué no usamos el lector de Google (GmsBarcodeScanning): no permite elegir la cámara.
// Aquí se juntan dos librerías:
//   - CameraX: abre la cámara y muestra la vista previa (PreviewView).
//   - ML Kit (barcode-scanning): mira cada imagen que entrega la cámara y busca un QR.
//
// activo:   si es false el lector sigue mostrando la cámara pero NO lee (se usa mientras se
//           muestra el resultado verde o rojo, para no leer el mismo QR varias veces).
// onQrLeido: se llama con el texto del QR cada vez que se lee uno.
@OptIn(ExperimentalGetImage::class)
@Composable
fun LectorQrFrontal(
    activo: Boolean,
    onQrLeido: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current


    val duenoDelCiclo = contexto as LifecycleOwner



    val vistaPrevia = remember { PreviewView(contexto) }


    val activoActual by rememberUpdatedState(activo)
    val alLeer by rememberUpdatedState(onQrLeido)


    DisposableEffect(Unit) {

        val ejecutor = Executors.newSingleThreadExecutor()


        val lector = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        .
        val futuroCamara = ProcessCameraProvider.getInstance(contexto)
        futuroCamara.addListener({
            val camara = futuroCamara.get()


            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(vistaPrevia.surfaceProvider)

            // viejas si el lector va lento, para trabajar siempre con la más reciente.
            val analisis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analisis.setAnalyzer(ejecutor) { imagen ->
                val foto = imagen.image
                if (foto == null || !activoActual) {
                    // Sin imagen, o lector en pausa: se suelta la imagen y se sigue.
                    imagen.close()
                } else {

                    val entrada = InputImage.fromMediaImage(foto, imagen.imageInfo.rotationDegrees)
                    lector.process(entrada)
                        .addOnSuccessListener { codigos ->
                            val texto = codigos.firstOrNull()?.rawValue
                            if (texto != null) {
                                alLeer(texto)
                            }
                        }

                        .addOnCompleteListener { imagen.close() }
                }
            }


            camara.unbindAll()
            camara.bindToLifecycle(
                duenoDelCiclo,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                analisis
            )
        }, ContextCompat.getMainExecutor(contexto))

        onDispose {
            try {
                futuroCamara.get().unbindAll()
            } catch (e: Exception) {

            }
            ejecutor.shutdown()
            lector.close()
        }
    }

    AndroidView(factory = { vistaPrevia }, modifier = modifier)
}