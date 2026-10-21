# AppJuntaVecinos

Aplicación Android para administrar los arriendos de las salas de una junta de vecinos y registrar sus pagos. El caso es ficticio: Junta de Vecinos Población Zaror, Los Jazmines 425, Cerrillos.

| | |
|---|---|
| Asignatura | DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC |
| Evaluación | Parcial 2 |
| Integrantes | Pablo Alexander Rico Rodríguez, William Martín Rodríguez Troncoso |
| Planificación | [Tablero de Trello](https://trello.com/b/Cb8xIwiv/appjuntavecinos-parcial-2) |

## Qué hace

Los vecinos piden reservas del Salón Principal o de la Sala Multiuso. Cuando la reserva está confirmada y pagada, reciben un ticket con código QR. La directiva confirma o rechaza reservas, registra pagos, revisa las finanzas y valida los QR en la entrada. Una tablet fija en una sala puede validar los QR por sí sola (modo puerta).

| Usuario | Puede |
|---|---|
| Vecino/a | Pedir reservas, ver su estado, cancelar las pendientes, ver la disponibilidad y las finanzas de la junta, y mostrar su ticket QR |
| Secretaría | Confirmar o rechazar las reservas pendientes |
| Tesorería | Registrar pagos, con comprobante de cámara o galería |
| Presidencia | Solo mirar |

Los tres cargos de la directiva ven reservas, pagos y resumen financiero, y pueden escanear QR.

## Funcionalidades implementadas

| Pantalla | La usa | Qué hace |
|---|---|---|
| Login | Todos | Entra con correo y clave. La clave se guarda como hash SHA-256. Se cierra sesión desde el menú de la cuenta |
| Mis reservas | Vecino | Lista de reservas con su estado y color. Al tocar una se abre su detalle |
| Detalle de reserva | Vecino | Cancela si está pendiente. Abre el ticket si está confirmada y pagada |
| Nueva reserva | Vecino | Formulario con validación de fecha, horas, horario de la sede y horario ocupado |
| Ticket QR | Vecino | Muestra el QR, la ventana de ingreso y un código corto |
| Disponibilidad | Vecino y directiva | Bloques horarios libres, confirmados y solicitados por sala y fecha. La directiva ve quién pidió cada uno |
| Finanzas | Vecino | Ingresos, gastos, saldo y gastos por categoría del mes elegido, solo lectura |
| Reservas | Directiva | La Secretaría confirma o rechaza. Al confirmar se revisa de nuevo el choque de horario |
| Pagos | Directiva | Lista de ingresos y gastos, y aviso de las reservas confirmadas que faltan por pagar |
| Registrar pago | Tesorería | Formulario para un pago de arriendo o de un consumo básico, con comprobante opcional |
| Resumen | Directiva | Lo mismo que Finanzas |
| Escanear | Directiva | Valida un QR con la cámara o un código corto escrito a mano |
| Modo puerta | Aparato fijo | Valida QR con la cámara frontal, sin que nadie de la directiva esté presente |

La app sigue el modo claro u oscuro del teléfono. En pantallas anchas la barra de navegación pasa a un panel lateral y el contenido se centra con un máximo de 640 dp. El Resumen, además, acomoda sus tarjetas en fila.

## Cómo cumple la pauta

| Se pide | Dónde está |
|---|---|
| Interfaz organizada y navegación clara | Material Design 3. Barra de navegación inferior que pasa a panel lateral en pantallas anchas (`NavigationSuiteScaffold`) y un `NavHost` con 12 rutas en `navigation/`. Modo claro y oscuro |
| Formularios validados con íconos y mensajes visuales | Login, Nueva reserva y Registrar pago, más el código de Escanear y la clave del modo puerta. Cada campo con error muestra borde rojo, mensaje y el ícono de `IconoDeError`. Los formularios incluyen checkbox y switch |
| Validaciones desde la lógica, no desde la interfaz | Las reglas están en los `ViewModel` y en `utils/`. La pantalla solo recibe un `UiState` (`data/model/*UiState.kt`) con los errores por campo y los muestra |
| Animaciones funcionales | `animateColorAsState`: la tarjeta de una reserva cambia de color cuando cambia su estado (Mis reservas y Reservas). `AnimatedVisibility`: en Registrar pago los campos aparecen y desaparecen según el tipo de pago y el switch del comprobante |
| Estructura modular y persistencia local | MVVM separado en carpetas (`ui`, `viewmodel`, `data`, `utils`, `navigation`). Room guarda usuarios, dependencias, reservas, pagos e ingresos a la puerta (5 entidades, base de datos versión 3). DataStore guarda la configuración del modo puerta |
| Repositorio en GitHub y planificación en Trello | Historial de commits de los dos integrantes, con ramas por funcionalidad. Las tareas están en el tablero de Trello enlazado arriba |
| Al menos dos recursos nativos | Cámara y galería (comprobante del pago), lector de códigos de Google y cámara frontal con CameraX (QR). Detalle en la sección Recursos nativos |

## Reglas de negocio

- La fecha de una reserva no puede ser pasada. Las horas van de 09:00 a 22:00 y la de fin es posterior a la de inicio.
- No se puede reservar un horario ocupado por una reserva confirmada de la misma sala. Se revisa al pedirla y otra vez al confirmarla.
- Solo se cancela una reserva pendiente. Solo la Secretaría confirma o rechaza, y solo las pendientes.
- Solo la Tesorería registra pagos. Un pago de arriendo exige una reserva confirmada y cada reserva tiene como máximo uno.
- El monto es mayor que 0 y tiene hasta 9 dígitos. La fecha del pago no puede ser futura. Un pago es informativo: no procesa transacciones reales.
- El QR solo existe si la reserva está confirmada y pagada.
- Las fechas se muestran como día-mes-año, por ejemplo 21-10-2026, y se guardan como año-mes-día para poder ordenarlas y compararlas.

## QR y modo puerta

**Ticket.** El QR lleva la reserva, la sala, la fecha, las horas, el nombre del vecino y una firma (un hash de esos datos más una clave de la app). La pantalla muestra además la ventana de ingreso, por ejemplo "de 17:50 a 18:15", y un código corto como `1-5267E2`.

**Escanear.** La directiva lee el QR con la cámara o escribe el código corto. La app responde:

| Respuesta | Cuándo |
|---|---|
| Acceso permitido | La firma es válida y es el día de la reserva, entre 10 minutos antes y 15 después de la hora de inicio |
| Fuera de horario | La firma es válida, pero es otro día u otra hora |
| Código no válido | No es un QR de la junta, o alguien cambió un dato |

**Modo puerta.** Un aparato, por ejemplo una tablet fija en una sala, valida los QR sin supervisión.

- Para activarlo, en el Login se toca 7 veces seguidas el título "Inicia sesión", con menos de 2 segundos entre toques. Se elige la sala y una clave para salir, que se guarda como hash. Desde entonces el aparato arranca siempre en "Acerque su QR" y usa la cámara frontal.
- El QR pasa 4 revisiones: firma válida, sala de esa puerta, ventana de ingreso y que no se haya usado antes. Si pasa, la pantalla se pone verde con "Puerta abierta". Si no, se pone roja con el motivo. El aviso se quita solo a los 4 segundos. La puerta es simulada.
- Para salir se repite el gesto de 7 toques sobre "Acerque su QR" y se escribe la clave elegida.

## Recursos nativos

| Recurso | Para qué | Detalle |
|---|---|---|
| Cámara | Foto del comprobante de un pago | Pide el permiso `CAMERA`. Si se niega, la app avisa y deja usar la galería |
| Galería | Elegir un comprobante ya guardado | No pide permiso. El archivo se copia a la carpeta privada de la app |
| Lector de códigos de Google | Escanear el QR en la pestaña Escanear | Usa la pantalla de lectura de Google Play Services |
| Cámara frontal (CameraX y ML Kit) | Leer el QR en el modo puerta | El lector de Google no permite elegir la cámara |

## Cómo ejecutarla

Requisitos: una versión de Android Studio que soporte el Android Gradle Plugin 9.4.1 (el wrapper trae Gradle 9.6.0), internet la primera vez y un aparato o emulador con Android 7.0 (API 24) o superior.

1. Clonar el repositorio: `git clone https://github.com/PabloRico25/AppJuntaVecinos.git`
2. Abrir la carpeta con Android Studio y esperar a que termine la sincronización de Gradle.
3. Conectar un celular con la depuración USB activada, o crear un dispositivo en Device Manager.
4. Ejecutar el módulo `app`.

La base de datos se llena sola la primera vez con 2 dependencias, 6 usuarios, 8 reservas y 6 pagos. Las fechas de esos datos son relativas al día de instalación. Si el aparato ya tenía otra versión de la app, hay que desinstalarla o borrar sus datos antes de abrir esta, porque la base de datos cambió de versión.

**Usuarios de prueba.** Todos los datos son ficticios y la clave de todos es `1234`.

| Correo | Rol |
|---|---|
| juan.perez@zaror.test | Vecino |
| ana.munoz@zaror.test | Vecina (empieza sin reservas) |
| pedro.soto@zaror.test | Vecino |
| maria.torres@zaror.test | Directiva, Secretaría |
| luis.rojas@zaror.test | Directiva, Tesorería |
| carmen.vidal@zaror.test | Directiva, Presidencia |

## Estructura del proyecto

```
AppJuntaVecinos/
├─ app/
│  ├─ build.gradle.kts              módulo: SDK mínimo 24, objetivo 37, dependencias
│  └─ src/
│     └─ main/
│        ├─ AndroidManifest.xml     permiso de cámara y actividad principal
│        ├─ res/                    íconos, colores, textos, tema y rutas de archivos
│        └─ java/com/example/appjuntavecinos/
│           ├─ MainActivity.kt
│           ├─ navigation/
│           │  ├─ AppNavigation.kt  NavHost, barra de navegación y contenido centrado
│           │  ├─ Destinos.kt       pestañas del vecino y de la directiva
│           │  └─ Rutas.kt          nombre de cada ruta
│           ├─ ui/
│           │  ├─ theme/            Color.kt, Theme.kt (claro y oscuro), Type.kt
│           │  ├─ components/       ActivarPuertaDialog, ContenidoCentrado, IconoDeError,
│           │  │                    LectorQrFrontal, MenuDesplegable
│           │  └─ screens/          una pantalla por archivo (Login, MisReservas,
│           │                       DetalleReserva, TicketQr, NuevaReserva, Disponibilidad,
│           │                       ReservasDirectiva, Pagos, RegistrarPago, Resumen,
│           │                       Escanear y Puerta)
│           ├─ viewmodel/           un ViewModel por flujo: estado y validaciones
│           ├─ utils/               hash, fechas, bloques horarios, QR, puerta,
│           │                       comprobante, resumen financiero y tamaño de pantalla
│           └─ data/
│              ├─ model/            entidades de Room y estados de formulario (UiState)
│              └─ repository/       DAOs, AppDatabase, PuertaPreferencias y JuntaRepository
├─ gradle/libs.versions.toml        versiones de todas las dependencias
├─ build.gradle.kts, settings.gradle.kts, gradlew y gradlew.bat
└─ README.md
```

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 2.2.10 | Lenguaje |
| Jetpack Compose con Material 3 | BOM 2026.02.01 | Interfaz, tema claro y oscuro, Window Size Classes |
| Navigation Compose | 2.10.2 | Navegación entre pantallas |
| ViewModel y StateFlow | Lifecycle 2.11.0 | Estado y validaciones |
| Room con KSP | 2.8.5 y 2.3.12 | Base de datos local |
| DataStore | 1.2.1 | Configuración del modo puerta |
| Coil | 2.7.0 | Vista previa del comprobante |
| ZXing | 3.5.3 | Dibuja el código QR |
| Lector de códigos de Google | 16.1.0 | Escanea el QR |
| CameraX y ML Kit | 1.6.2 y 17.3.0 | Lee el QR con la cámara frontal |
| Android Gradle Plugin y Gradle | 9.4.1 y 9.6.0 | Compilación |

## Piezas que no se vieron en clase

Las integramos por nuestra cuenta. Las herramientas externas, con el permiso del docente. En el código cada una lleva un comentario `No visto en clase`.

| Pieza | Dónde | Para qué |
|---|---|---|
| ZXing | `utils/QrImagen.kt` | Dibuja el código QR |
| Lector de códigos de Google (ML Kit) | `ui/screens/EscanearScreen.kt` | Escanea el QR |
| CameraX y ML Kit | `ui/components/LectorQrFrontal.kt` | Lee el QR con la cámara frontal en el modo puerta |
| SHA-256 con `MessageDigest` | `utils/ClaveUtils.kt` | Guarda las claves como hash |
| `Patterns.EMAIL_ADDRESS` | `viewmodel/LoginViewModel.kt` | Valida el formato del correo |
| Copia a `filesDir` | `utils/ComprobanteUtils.kt` | Guarda el comprobante en la carpeta privada |
| `BackHandler` y `keepScreenOn` | `ui/screens/PuertaScreen.kt` | Bloquean el botón atrás y mantienen la pantalla encendida |
| `verticalScroll` | Disponibilidad, Escanear, Nueva reserva, Registrar pago, Resumen y Ticket QR | Permite recorrer la pantalla hacia abajo |
| `combine` | Disponibilidad, Pagos y Resumen | Junta varios flujos de datos en uno |
| `navArgument` | `navigation/AppNavigation.kt` | Pasa el número de la reserva al Detalle y al Ticket QR |
| `darkColorScheme` e `isSystemInDarkTheme()` | `ui/theme/Theme.kt` | Definen la paleta oscura y eligen la clara o la oscura según el modo del teléfono |
| `widthIn` | `ui/components/ContenidoCentrado.kt` | Limita el ancho máximo del contenido en pantallas grandes |

## Limitaciones

- La firma del QR es de demostración. La clave está dentro de la app y el repositorio es público, así que alguien que lea el código podría fabricar un QR. Para un uso real haría falta un servidor.
- Cada celular guarda sus datos en su propia base (Room y DataStore) y no se comparten entre aparatos. Por eso el código corto solo sirve en el aparato donde existe la reserva. Entre aparatos solo sirve el QR.
- El lector de códigos de Google necesita Google Play Services. Si no está, se puede escribir el código corto.
- El modo puerta es simulado y no abre ninguna cerradura. La clave de salida se escribe con el teclado normal del celular, que a veces agrega una mayúscula o un espacio de más.
- Solo el Resumen cambia su disposición según el ancho de la pantalla. En el resto el contenido se limita a 640 dp de ancho y se centra.
- El resumen financiero muestra totales, no una lista de movimientos. El detalle de cada pago está en Pagos, que solo ve la directiva.