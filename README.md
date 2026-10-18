# AppJuntaVecinos

Aplicación Android para la Junta de Vecinos Población Zaror, Los Jazmines 425,
Cerrillos. Caso ficticio para DSY1105, Evaluación Parcial 2. Versión de William.

## Integrantes

- Pablo Alexander Rico Rodríguez
- William Martín Rodríguez Troncoso
- Número de equipo: por confirmar

## Funcionalidades

La app contiene las 11 pantallas del caso y la ampliación de modo puerta.

| Pantalla | Qué permite |
|---|---|
| Login | Validar campos, iniciar/cerrar sesión y acceder según rol |
| Mis reservas | Consultar solicitudes propias y estado vacío |
| Nueva reserva | Elegir sala, fecha, horario y aceptar reglamento |
| Disponibilidad | Ver Libre, Solicitado y Ocupado por sala y fecha |
| Detalle de reserva | Revisar estado, cancelar una pendiente propia y abrir ticket |
| Ticket QR | Mostrar QR y código corto de una reserva confirmada y pagada |
| Reservas de directiva | Consultar todas; Secretaría confirma o rechaza |
| Pagos | Consultar registros, comprobantes y reservas por cobrar |
| Registrar pago | Tesorería registra arriendo o consumo, con foto/galería opcional |
| Resumen financiero | Ingresos, gastos, saldo y categorías por mes; consulta de vecinos sin nombres |
| Escanear | Leer QR o código corto y validar sala, fecha y ventana de ingreso |
| Modo puerta (ampliación) | Cámara frontal, clave de salida y un uso por ticket en ese aparato |

## Ejecutar

1. Abrir el proyecto en Android Studio y pulsar **Sync Now**.
2. Usar el JDK de Gradle configurado por Android Studio (daemon Java 25).
3. Instalar SDK 37. El dispositivo debe tener Android 7.0/API 24 o superior.
4. Seleccionar `app`, elegir emulador/teléfono y pulsar **Run**.
5. El lector de la pestaña Escanear necesita Google Play Services; su primer uso
   puede necesitar conexión para descargar el módulo. La puerta usa ML Kit incluido en la app.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:lintDebug
```

Las pruebas Android se ejecutan en un emulador de pruebas. Gradle puede reinstalar
la aplicación; no usar un dispositivo con datos que se necesiten conservar para ejecutar la suite.

## Cuentas ficticias

La clave de todas es **1234**. Las cuentas se documentan aquí, no en el Login.

| Correo | Rol / cargo |
|---|---|
| juan.perez@zaror.test | Vecino |
| ana.munoz@zaror.test | Vecina; comienza sin reservas en una instalación nueva |
| pedro.soto@zaror.test | Vecino |
| maria.torres@zaror.test | Directiva, Secretaría |
| luis.rojas@zaror.test | Directiva, Tesorería |
| carmen.vidal@zaror.test | Directiva, Presidencia |

## Recorrido completo para estudiar y demostrar

1. Ana: enviar Nueva reserva vacía para ver los errores. Luego elegir sala,
   fecha y un horario libre, aceptar reglamento y enviar.
2. La solicitud aparece Pendiente. Desde su detalle se puede cancelar mientras siga pendiente.
3. María: entrar a Reservas y confirmar la solicitud. Una confirmada ocupa el horario.
4. Luis: Pagos → Registrar pago → Arriendo → elegir esa reserva → monto entero
   positivo → fecha de hoy o anterior. Descripción y comprobante son opcionales.
5. Activar Adjuntar comprobante para probar **Tomar foto** o **Elegir de galería**.
   Confirmar el registro informativo y guardar. Revisar Ver comprobante en Pagos.
6. Ana: abrir el detalle de esa reserva y **Ver ticket QR**.
7. Otra sesión de directiva: Escanear → elegir la sala → escanear el QR.
   El código corto funciona solo donde está guardada esa reserva.
8. Probar también sala incorrecta, ticket fuera de horario y código inventado.
9. Registrar un Consumo básico con Luis y comprobar el cambio en Resumen.
10. Cerrar y abrir la app: los datos siguen guardados, la sesión pide Login otra vez.

Solo las confirmadas bloquean horario. Dos pendientes pueden coincidir: se vuelve
a comprobar el cruce al confirmar. Una reserva de 10:00 a 12:00 permite otra desde las 12:00.
Solo se registra un pago de arriendo por reserva, incluso ante dos guardados simultáneos.

## Modo puerta en una tablet

1. Cerrar sesión. Tocar **Inicia sesión** siete veces seguidas (sin pausas mayores a dos segundos).
2. Seleccionar la sala y definir una clave de al menos cuatro caracteres. Recordarla para salir.
3. Pulsar Activar y conceder permiso de cámara. Se utiliza la **cámara frontal**.
4. Acercar un QR válido de esa sala. El resultado ocupa la pantalla durante cuatro segundos.
5. Retirar el QR y volver a mostrarlo: un ticket aceptado queda usado en esa tablet.
6. Para salir, tocar siete veces **Acerque su QR** o usar Atrás; ambos piden la clave.

La configuración se conserva al reiniciar. Es una puerta **simulada**; no mueve una
cerradura y no impide salir de Android mediante sus controles del sistema.

La ventana de ingreso es desde **10 minutos antes hasta 15 minutos después del inicio**,
incluidos ambos límites. Para una reserva de 18:00 a 20:00: desde 17:50 hasta 18:15;
a las 18:16 se rechaza aunque la reserva todavía no haya terminado. Las horas del
formulario son enteras: preparar la demostración cerca del inicio, sin cambiar el reloj.

## Demostración con tres aparatos

- Aparato 1: crear con Ana, confirmar con María, pagar con Luis y mostrar el ticket con Ana.
- Aparato 2: entrar como directiva y escanear el QR completo.
- Tablet: activar la puerta de la misma sala y leer el QR con la cámara frontal.

No hay sincronización. El QR completo lleva sus datos y una firma de demostración,
por eso puede leerse en otro aparato. El código corto necesita la base local original.
El registro de uso es local a cada puerta: dos tablets independientes no comparten
los tickets usados. Las reservas creadas independientemente pueden repetir identificadores;
para la demo, emitir todos los tickets desde el aparato 1.

## Estructura y estudio

```text
app/src/main/java/com/example/appjuntavecinos/
  MainActivity.kt
  navigation/       AppNavigation, Rutas y Destinos
  data/model/       entidades, UiState y errores
  data/repository/  DAO, AppDatabase, JuntaRepository y PuertaPreferencias
  viewmodel/        estado, validaciones y acciones
  ui/screens/       pantallas Compose
  ui/components/    formularios y lector frontal
  ui/theme/         colores, tipografía y tema
  utils/            fechas, claves, QR, comprobantes y reglas de ingreso
```

Comenzar por [Guía para estudiar y defender el proyecto](docs/GUIA_DEL_PROYECTO.md).
La evidencia de comprobaciones está en [Pruebas de esta versión](docs/PRUEBAS.md).

## Persistencia y límites del caso

- Room guarda cinco tablas en `junta.db`, esquema 3. La migración 2 → 3 añade
  IngresoEntity conservando usuarios, reservas y pagos; no borra datos para actualizar.
- Carga inicial: seis usuarios, dos salas, ocho reservas y cinco pagos ficticios.
  Sus fechas se calculan respecto a la primera carga y no se actualizan a diario.
- DataStore guarda sala, modo puerta y hash de su clave. El Login no persiste la sesión.
- Los comprobantes se copian a archivos privados; no dependen de conservar el original
  de la galería. Si se desinstala la app o se borran sus datos, se puede perder esta información.
- SHA-256 y la firma compartida del QR sirven para el caso académico. No constituyen
  autenticación bancaria ni un sistema de acceso de producción.
- Sin backend, un lector externo no conoce cambios posteriores al ticket ni pagos remotos.
- La app no realiza cobros. Todos los pagos son registros informativos en pesos chilenos.
- La entrega requiere además la defensa individual y evidencias reales de GitHub/Trello.
