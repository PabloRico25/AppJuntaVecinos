# Reglas de R8 para el APK de release.

# Gson convierte el JSON a objetos usando los nombres de los campos. Si R8 los renombra, la app deja de
# entender al servidor. Por eso las clases de data/model conservan sus nombres.
-keep class com.example.appjuntavecinos.data.model.** { *; }

# Gson lee los tipos genericos (por ejemplo List<ReservaEntity>) desde estos atributos.
-keepattributes Signature
-keepattributes *Annotation*
