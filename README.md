# AppJuntaVecinos

App Android para gestionar los arriendos de las dependencias comunitarias de la Junta de Vecinos Población Zaror (Los Jazmines 425, Cerrillos) y registrar sus pagos. Es un caso ficticio y un MVP académico.

Evaluación Parcial 2 de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes

- Pablo Alexander Rico Rodríguez
- William Martín Rodríguez Troncoso

## Qué hace la app

Se entra con correo y clave, y hay dos tipos de usuario:

- **Vecino/a:** pide reservas de una dependencia (Salón Principal o Sala Multiuso), ve el estado y el detalle de cada una, cancela las pendientes, revisa la disponibilidad, consulta las finanzas de la junta y, cuando una reserva está confirmada y pagada, muestra su ticket con el código QR.
- **Directiva:** ve las reservas, los pagos y el resumen financiero, y escanea los QR de los vecinos. Según el cargo, la Secretaría confirma o rechaza reservas, la Tesorería registra pagos y la Presidencia solo mira.

Además, un aparato puede quedar como puerta de una sala y validar los QR por sí solo (modo puerta, más abajo).

## Funcionalidades

- Login con correo y clave (la clave se guarda como hash SHA-256) y cierre de sesión desde el menú de la cuenta.
- Mis reservas, con el estado de cada una y su color. Al tocar una se abre su detalle: ahí se cancela si está pendiente, o se abre el ticket si está confirmada y pagada.
- Nueva reserva, con validación de fecha, horas, horario de la sede y horario ocupado.
- Disponibilidad por dependencia y fecha: franjas libres, confirmadas y solicitadas. El vecino la ve sin nombres; la directiva ve quién pidió cada una.
- Reservas de la directiva: la Secretaría confirma o rechaza, y al confirmar se vuelve a revisar el choque de horario.
- Pagos de la directiva, con la lista de ingresos y gastos y un aviso de las reservas confirmadas que faltan por pagar. La Tesorería registra los pagos, con comprobante de cámara o galería.
- Resumen financiero (directiva) y Finanzas (vecino, solo lectura): ingresos, gastos, saldo y gastos por categoría del mes elegido. Se cambia de mes con botones, sin pasar del mes actual, y el diseño cambia según el ancho de la pantalla.
- Ticket QR, escaneo y modo puerta, que se explican a continuación.

## Ticket QR y escaneo

El detalle de una reserva confirmada y pagada ofrece Ver ticket QR. La pantalla muestra los datos de la reserva, el QR, la ventana de ingreso (por ejemplo "de 17:50 a 18:15") y un código corto como `1-5267E2`. El QR lleva la reserva, la sala, la fecha, las horas, el nombre del vecino y una firma (un hash de esos datos más una clave de la app).

En la pestaña Escanear, la directiva lee el QR con la cámara o escribe el código corto, y la app responde:

- **Acceso permitido:** la firma es válida y es el día de la reserva, entre 10 minutos antes y 15 después de la hora de inicio.
- **Fuera de horario:** la firma es válida, pero es otro día u otra hora.
- **Código no válido:** no es un QR de la junta, o alguien cambió un dato.

## Modo puerta

Un aparato, por ejemplo una tablet fija en una sala, puede validar el QR de los vecinos sin que nadie de la directiva esté ahí.

- **Activar:** en el Login se toca 7 veces seguidas el título "Inicia sesión" (menos de 2 segundos entre toques). Se elige la sala y una clave para salir, que se guarda como hash. Desde ahí el aparato arranca siempre en modo puerta ("Acerque su QR"), con su cámara frontal.
- **Abrir:** el QR tiene que pasar 4 revisiones: firma válida, sala de esa puerta, ventana de ingreso y que no se haya usado antes. Si pasa, la pantalla se pone verde con "Puerta abierta"; si no, roja con el motivo. El aviso se quita solo a los 4 segundos. La puerta es simulada.
- **Salir:** el mismo gesto de 7 toques sobre "Acerque su QR" y la clave que se eligió.

## Reglas de negocio

- La fecha de una reserva no puede ser pasada. Las horas van de 09:00 a 22:00 y la de fin es mayor que la de inicio.
- No se puede reservar un horario ocupado por una reserva confirmada de la misma dependencia.
- Solo se cancela una reserva pendiente. Solo la Secretaría confirma o rechaza, y solo las pendientes.
- Solo la Tesorería registra pagos. Un pago de arriendo exige una reserva confirmada, y cada reserva tiene como máximo uno.
- El monto de un pago es mayor que 0 y su fecha no es futura. Un pago es solo informativo: no procesa transacciones reales.
- El QR solo existe si la reserva está confirmada y pagada.

## Recursos nativos

- **Cámara:** toma la foto del comprobante. Pide el permiso `CAMERA` y, si se niega, la app avisa y deja usar la galería.
- **Galería:** permite elegir un comprobante ya guardado, sin pedir permiso. El comprobante se copia a la carpeta privada de la app.
- **Lector de códigos de Google:** escanea el QR en la pestaña Escanear, con su propia pantalla de lectura.
- **Cámara propia (CameraX y ML Kit):** el modo puerta usa la cámara frontal, que el lector de Google no permite elegir.

## Tecnologías

Kotlin, Jetpack Compose con Material Design 3, MVVM con StateFlow, Room (con KSP), Navigation Compose, DataStore, Window Size Classes, Coil, ZXing, CameraX y ML Kit.

## Piezas que no se vieron en clase

Las integramos por nuestra cuenta, con el permiso del docente para usar herramientas externas. En el código cada una lleva un comentario `No visto en clase`.

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

## Estructura del proyecto

```
app/src/main/java/com/example/appjuntavecinos/
├─ MainActivity.kt
├─ navigation/     rutas, destinos de cada menú y NavHost
├─ ui/
│   ├─ theme/      colores, tema y tipografía
│   ├─ components/ menú desplegable, lector de QR y diálogo de la puerta
│   └─ screens/    una pantalla por archivo
├─ viewmodel/      estado y validaciones de cada flujo
├─ utils/          hash, fechas, comprobante, QR, franjas horarias y ancho de ventana
└─ data/
    ├─ model/      entidades de Room y estados de formulario con sus errores
    └─ repository/ DAOs, base de datos, preferencias del modo puerta y repositorio
```

## Cómo ejecutarla

1. Clonar el repositorio y abrirlo con Android Studio.
2. Esperar a que termine la sincronización de Gradle (necesita internet la primera vez).
3. Conectar un celular con la depuración USB activada, o crear un dispositivo en Device Manager.
4. Ejecutar el módulo `app`. Funciona desde Android 7.0 (API 24).

La base de datos local se llena sola la primera vez con 2 dependencias, 6 usuarios, 8 reservas y 6 pagos. Las fechas de esos datos son relativas al día en que se instala la app. Si el aparato ya tenía otra versión de la app, hay que desinstalarla o borrar sus datos antes de abrir esta, porque la base de datos cambió de versión.

Hay 36 pruebas automáticas en `app/src/test`. En Android Studio: clic derecho sobre la carpeta `test` y Run Tests.

## Usuarios de prueba

Todos los datos son ficticios. La clave de todos es `1234`.

| Correo | Rol |
|---|---|
| juan.perez@zaror.test | Vecino |
| ana.munoz@zaror.test | Vecina (empieza sin reservas) |
| pedro.soto@zaror.test | Vecino |
| maria.torres@zaror.test | Directiva, Secretaría |
| luis.rojas@zaror.test | Directiva, Tesorería |
| carmen.vidal@zaror.test | Directiva, Presidencia |

## Limitaciones

- La firma del QR es de demostración: la clave está dentro de la app y el repositorio es público, así que alguien que lea el código podría fabricar un QR. Para un uso real haría falta un servidor.
- Cada celular guarda sus datos en su propia base (Room y DataStore) y no se comparten entre aparatos: lo que un cargo registra en su teléfono no le llega a otro. Por eso el código corto solo sirve en el aparato donde existe la reserva; entre aparatos solo sirve el QR.
- El lector de códigos de Google necesita Google Play Services. Si no está, se puede escribir el código corto.
- El modo puerta es simulado: no abre ninguna cerradura. La clave de salida se escribe con el teclado normal del celular, que a veces pone una mayúscula o un espacio de más.
- El diseño por tamaño de ventana solo se aplicó al Resumen; en el resto de la app solo cambia la barra de navegación.
- El resumen financiero muestra totales, no una lista de movimientos. El detalle de cada pago está en Pagos, que solo ve la directiva.