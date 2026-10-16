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

    // CameraX necesita un "dueño del ciclo de vida": cuando la app se pausa, la cámara se
    // apaga sola y cuando vuelve, se enciende. La Activity (MainActivity) cumple ese papel.
    val duenoDelCiclo = contexto as LifecycleOwner

    // PreviewView es la vista de Android que dibuja lo que ve la cámara.
    // Compose no la trae, por eso se mete dentro con AndroidView.
    val vistaPrevia = remember { PreviewView(contexto) }

    // rememberUpdatedState: el analizador de abajo se crea una sola vez, pero estos dos
    // valores pueden cambiar. Así el analizador siempre ve el valor más reciente.
    val activoActual by rememberUpdatedState(activo)
    val alLeer by rememberUpdatedState(onQrLeido)

    // DisposableEffect: se ejecuta al entrar a la pantalla y, cuando la pantalla se va,
    // corre onDispose para soltar la cámara y el lector (si no, quedarían abiertos).
    DisposableEffect(Unit) {
        // Un hilo aparte para analizar imágenes, así la pantalla no se congela.
        val ejecutor = Executors.newSingleThreadExecutor()

        // El lector de ML Kit, configurado para buscar solo códigos QR.
        val lector = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        // Pedir la cámara es asíncrono: devuelve un "futuro" y se avisa cuando está lista.
        val futuroCamara = ProcessCameraProvider.getInstance(contexto)
        futuroCamara.addListener({
            val camara = futuroCamara.get()

            // Caso de uso 1: la vista previa, que se dibuja en la PreviewView.
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(vistaPrevia.surfaceProvider)

            // Caso de uso 2: el análisis. STRATEGY_KEEP_ONLY_LATEST descarta las imágenes
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
                    // rotationDegrees le dice a ML Kit cuánto está girada la imagen.
                    val entrada = InputImage.fromMediaImage(foto, imagen.imageInfo.rotationDegrees)
                    lector.process(entrada)
                        .addOnSuccessListener { codigos ->
                            val texto = codigos.firstOrNull()?.rawValue
                            if (texto != null) {
                                alLeer(texto)
                            }
                        }
                        // Siempre hay que cerrar la imagen al terminar; si no, la cámara
                        // se queda esperando y deja de entregar imágenes nuevas.
                        .addOnCompleteListener { imagen.close() }
                }
            }

            // Se sueltan usos anteriores y se conecta todo a la cámara FRONTAL.
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
                // Si la cámara nunca llegó a abrirse, no hay nada que soltar.
            }
            ejecutor.shutdown()
            lector.close()
        }
    }

    AndroidView(factory = { vistaPrevia }, modifier = modifier)
}