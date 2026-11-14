# Reglas de R8 para el APK de release.

# Gson convierte el JSON a objetos usando los nombres de los campos. Si R8 los renombra, la app deja de
# entender al servidor. Por eso las clases de data/model conservan sus nombres.
-keep class com.example.appjuntavecinos.data.model.** { *; }

# Gson lee los tipos genericos (por ejemplo List<ReservaEntity>) desde estos atributos.
-keepattributes Signature
-keepattributes *Annotation*

# El lector de QR de la puerta usa ML Kit (barcode) y CameraX. Con la optimizacion de R8 activa, ML Kit se rompia:
# la app se cerraba con un NullPointerException al abrir la camara de la puerta, solo en el APK de release.
# Se conservan sus clases tal como vienen.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common.** { *; }
-dontwarn com.google.mlkit.**
