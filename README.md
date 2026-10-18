# AppJuntaVecinos

App Android para gestionar los arriendos de las dependencias comunitarias de la Junta de Vecinos Población Zaror (Los Jazmines 425, Cerrillos) y registrar sus pagos. Es un caso ficticio y un MVP académico.

Evaluación Parcial 2 de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes

- Pablo Alexander Rico Rodríguez
- William Martín Rodríguez Troncoso

## Qué hace la app

Hay dos tipos de usuario, que entran por un login con correo y clave:

- **Vecino/a:** pide reservas de una dependencia (Salón Principal o Sala Multiuso), ve el estado de sus solicitudes y abre el detalle de cada una, cancela las que siguen pendientes, revisa la disponibilidad de horarios, consulta las finanzas de la junta (solo lectura) y, cuando una reserva está confirmada y pagada, muestra su ticket con el código QR.
- **Directiva:** ve las reservas, los pagos y el resumen financiero, y escanea los QR de los vecinos. Según el cargo se habilitan los botones: la Secretaría confirma o rechaza reservas, la Tesorería registra pagos y la Presidencia solo mira.

Un tercer modo, sin login, convierte el aparato en una puerta que valida el QR sola (ver más abajo).

## Funcionalidades

- [x] Login con correo y clave, con validación por campo y la clave guardada como hash SHA-256
- [x] Cerrar sesión desde el menú de la cuenta
- [x] Mis reservas, con el estado de cada una (pendiente, confirmada, cancelada o rechazada) y su color; al tocar una reserva se abre su detalle
- [x] Nueva reserva, con validaciones (fecha, horas, horario de la sede, horario ocupado) y aviso al crearla
- [x] Detalle de reserva: estado, datos y fecha de solicitud; desde ahí se cancela una reserva pendiente (con confirmación) o se abre el ticket de una confirmada y pagada
- [x] Disponibilidad por dependencia y fecha, con las franjas libres, confirmadas y solicitadas
- [x] Reservas de la directiva: la Secretaría confirma o rechaza, y se vuelve a revisar el choque al confirmar
- [x] Pagos de la directiva, con la lista de ingresos y gastos y un aviso de reservas confirmadas que faltan por pagar
- [x] Registro de pagos con comprobante de cámara o galería (solo Tesorería)
- [x] Resumen financiero: ingresos, gastos, saldo y gastos por categoría, con botones para cambiar de mes y un diseño distinto según el ancho de la pantalla
- [x] Finanzas del vecino: el mismo resumen, en solo lectura
- [x] Ticket QR de una reserva confirmada y pagada: código QR firmado, datos de la reserva, ventana de ingreso y un código corto para escribir a mano
- [x] Escanear el QR: acceso permitido, fuera de horario o código no válido
- [x] Modo puerta: un aparato se convierte en lector fijo de una sala y valida el QR por su cuenta

Lo que aparece sin marcar no está en esta versión. Ver el apartado Limitaciones.

## Recursos nativos

- **Cámara:** toma la foto del comprobante de un pago. Pide el permiso `CAMERA` cuando se necesita y, si el usuario lo niega, la app no se cae: avisa y deja usar la galería.
- **Galería:** permite elegir una imagen ya guardada como comprobante. No necesita permiso.
- **Lector de códigos de Google:** la cámara para escanear el QR del vecino desde la pestaña Escanear. Abre su propia pantalla de lectura y no necesita el permiso `CAMERA` de la app.
- **Cámara propia (CameraX + ML Kit):** el modo puerta usa su propia cámara, la **frontal**, para mirar al vecino que se acerca. Se armó aparte porque el lector de Google no permite elegir qué cámara usar.

El comprobante elegido se copia a la carpeta privada de la app, para que siga disponible aunque la foto original desaparezca.

## El código QR

Cuando una reserva está confirmada y tiene su pago de arriendo registrado, el vecino ve en su detalle el botón Ver ticket QR, que abre la pantalla del ticket. El QR lleva dentro la reserva, la sala, la fecha, las horas, el nombre del vecino y una firma (un hash de esos datos más una clave de la app). La Secretaría lo escanea y la app responde:

- **Acceso permitido:** la firma es válida y es el día de la reserva, entre 10 minutos antes y 15 minutos después de la hora de inicio.
- **Fuera de horario:** la firma es válida, pero no es el día o la hora.
- **Código no válido:** no es un QR de la junta o alguien cambió un dato.

En la pantalla del ticket se muestra, debajo del QR, la ventana de ingreso (por ejemplo "de 17:50 a 18:15") y también un código corto, por ejemplo `1-5267E2`, que se puede escribir en la caja de la pantalla Escanear si no se puede usar la cámara. Ese código busca la reserva en la base de datos del aparato, así que solo funciona en el celular que la tiene.

## Modo puerta

Un aparato (por ejemplo una tablet fija en una sala) puede convertirse en una puerta que valida el QR del vecino sin que nadie de la directiva tenga que estar ahí.

**Cómo se activa:** en la pantalla de Login se toca 7 veces seguidas el título "Inicia sesión" (menos de 2 segundos entre toque y toque). Se abre un diálogo para elegir la sala de esa puerta y escribir una clave (se guarda como hash, nunca en texto). Desde ahí el aparato arranca siempre en modo puerta, con la pantalla "Acerque su QR" y su propia cámara frontal encendida.

**Las 4 revisiones que hace sola, antes de abrir:**

1. Que la firma del QR sea válida (la misma del escaneo normal).
2. Que la sala del QR sea la de esa puerta.
3. Que esté dentro de la ventana de ingreso (10 minutos antes a 15 después de la hora de inicio).
4. Que ese QR no se haya usado antes (guarda cada ingreso válido en su propia base).

Si las cuatro se cumplen, la pantalla se pone verde con "Puerta abierta" y el nombre del vecino; si falla alguna, se pone roja con el motivo. El aviso se quita solo a los 4 segundos. La puerta en sí es simulada: no abre nada físico.

**Para salir del modo puerta:** el mismo gesto de 7 toques, esta vez sobre el título "Acerque su QR", y la clave que se eligió al activarlo.

## Resumen financiero

La directiva (pestaña Resumen) y el vecino (pestaña Finanzas, en solo lectura) ven el mes elegido con los ingresos (pagos de arriendo), los gastos (consumos básicos), el saldo y los gastos separados por categoría. Parte en el mes actual; con los botones Anterior y Siguiente se cambia de mes, y no se puede avanzar más allá del mes actual. Se actualiza solo cuando se registra un pago nuevo. En pantallas más anchas (una tablet o el teléfono girado) las tarjetas se acomodan distinto: apiladas, en pareja o las tres en una fila.

## Disponibilidad

Tanto el vecino como la directiva pueden ver, por dependencia y fecha, qué horas están libres, cuáles ya tiene una reserva confirmada y cuáles tienen una solicitud pendiente. El vecino la ve sin nombres; la directiva, desde un ícono en la pantalla de Reservas, ve además quién la pidió.

## Reglas de negocio

- La fecha de una reserva no puede ser pasada.
- La hora de fin tiene que ser mayor que la de inicio, y las dos dentro del horario de la sede (de 09:00 a 22:00).
- No se puede reservar un horario que ya esté ocupado por una reserva confirmada de la misma dependencia.
- Solo se puede cancelar una reserva pendiente.
- Solo la Secretaría confirma o rechaza reservas, y solo las que están pendientes. Al confirmar se vuelve a revisar que el horario no choque con otra reserva ya confirmada.
- Solo la Tesorería registra pagos.
- Un pago de arriendo exige una reserva confirmada, y cada reserva tiene como máximo un pago de arriendo.
- El monto de un pago tiene que ser mayor que 0, y su fecha no puede ser futura.
- Un pago es solo informativo: no procesa transacciones reales.
- El QR solo se muestra si la reserva está confirmada y pagada.

## Tecnologías

Kotlin, Jetpack Compose, Material Design 3, MVVM con StateFlow, Room (con KSP), Navigation Compose, DataStore (preferencias del modo puerta), CameraX y ML Kit (lector de QR con la cámara frontal), Window Size Classes (diseño adaptable del Resumen), Coil para mostrar la imagen del comprobante, ZXing para dibujar el QR y el lector de códigos de Google (ML Kit) para escanearlo desde Escanear.

## Piezas que no se vieron en clase

Estas piezas no se trabajaron en clase. Las integramos por nuestra cuenta, con el permiso del docente para usar herramientas externas, y en el código cada una lleva un comentario `No visto en clase` que lo indica.

- **ZXing** (`utils/QrImagen.kt`): dibuja el código QR de la reserva.
- **Lector de códigos de Google, ML Kit** (`ui/screens/EscanearScreen.kt`): escanea el QR desde la pestaña Escanear.
- **CameraX y ML Kit** (`ui/components/LectorQrFrontal.kt`): la cámara frontal del modo puerta, que lee el QR del vecino que se acerca.
- **SHA-256 con `MessageDigest`** (`utils/ClaveUtils.kt`): guarda las claves como hash y no como texto. El docente aceptó cualquier hash.
- **`Patterns.EMAIL_ADDRESS`** (`viewmodel/LoginViewModel.kt`): valida el formato del correo en el Login.
- **Copia del comprobante a `filesDir`** (`utils/ComprobanteUtils.kt`): guarda la imagen del comprobante en la carpeta privada de la app, para que no se pierda.
- **`BackHandler` y `keepScreenOn`** (`ui/screens/PuertaScreen.kt`): en el modo puerta, bloquean el botón atrás y mantienen la pantalla encendida.
- **`verticalScroll`** (Disponibilidad, Escanear, Nueva reserva, Registrar pago y Resumen): permite recorrer la pantalla hacia abajo cuando el contenido no cabe.
- **`combine`** (Disponibilidad, Pagos y Resumen): junta varios flujos de datos en uno, para que las listas y los totales se actualicen solos.
- **`navArgument`** (`navigation/AppNavigation.kt`): pasa el número de la reserva a las pantallas de Detalle y de Ticket QR.

## Estructura del proyecto

```
app/src/main/java/com/example/appjuntavecinos/
├─ MainActivity.kt
├─ navigation/     rutas, destinos de cada menú y NavHost
├─ ui/
│   ├─ theme/      colores, tema y tipografía
│   ├─ components/ piezas reutilizables (menú desplegable, lector de QR, diálogo de la puerta)
│   └─ screens/    una pantalla por archivo
├─ viewmodel/      estado y validaciones de cada flujo
├─ utils/          hash de la clave, fechas, comprobante, código QR, franjas horarias y ancho de ventana
└─ data/
    ├─ model/      entidades de Room y estados de formulario con sus errores
    └─ repository/ DAOs, base de datos, preferencias del modo puerta y repositorio
```

## Cómo ejecutarla

1. Clonar el repositorio.
2. Abrir la carpeta con Android Studio.
3. Esperar a que termine la sincronización de Gradle (descarga las librerías, hace falta internet la primera vez).
4. Conectar un celular con la depuración USB activada, o crear un dispositivo en Device Manager.
5. Ejecutar el módulo `app`.

La app funciona desde Android 7.0 (API 24). La base de datos local se llena sola la primera vez con datos de ejemplo: 2 dependencias, 6 usuarios, 8 reservas y 6 pagos.
Si el celular ya tenía instalada una versión anterior de la app, hay que borrar sus datos (o desinstalarla) antes de abrir esta, porque la base de datos cambió de versión.

## Usuarios de prueba

Todos los datos son ficticios. La clave de todos es `1234`.

| Correo | Rol | Qué puede hacer |
|---|---|---|
| juan.perez@zaror.test | Vecino | Pedir, ver y cancelar sus reservas, ver la disponibilidad y las finanzas, y ver el ticket QR de la reserva confirmada y pagada |
| ana.munoz@zaror.test | Vecina | Igual que Juan; no tiene reservas al empezar |
| pedro.soto@zaror.test | Vecino | Igual que Juan |
| maria.torres@zaror.test | Directiva, Secretaría | Ver reservas, pagos y resumen, confirmar o rechazar reservas y escanear QR |
| luis.rojas@zaror.test | Directiva, Tesorería | Ver reservas, pagos y resumen, registrar pagos y escanear QR |
| carmen.vidal@zaror.test | Directiva, Presidencia | Ver reservas, pagos y resumen, y escanear QR |

## Limitaciones

- La firma del QR es de demostración: la clave está dentro de la app y el repositorio es público, así que alguien que lea el código podría fabricar un QR. Para un uso real haría falta un servidor.
- El lector de códigos de la pantalla Escanear necesita Google Play Services en el aparato. Si no está, se puede escribir el código a mano.
- El código corto solo funciona en el aparato donde existe la reserva; entre aparatos distintos solo sirve el QR.
- El modo puerta es simulado: no abre ninguna cerradura física, y la clave para salir de ese modo se escribe con el teclado normal del celular, que a veces pone una mayúscula o un espacio de más sin que se note.
- El resumen financiero muestra totales, no una lista de movimientos; el detalle de cada pago está en la pantalla Pagos, que ven solo los cargos de la directiva.
- Window Size Classes solo se aplicó a la pantalla Resumen, como ejemplo de diseño adaptable; el resto de la app usa el mismo diseño en cualquier tamaño.
- Los datos son ficticios; no hay información real de la organización ni de sus vecinos.
- Toda la información se guarda en el celular (Room y DataStore). Cada celular tiene su propia base de datos y no se comparten entre aparatos: lo que un cargo registra en su teléfono no le llega a otro cargo en el suyo.