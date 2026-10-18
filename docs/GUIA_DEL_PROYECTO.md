# Guía para estudiar y defender AppJuntaVecinos

Esta guía sigue el código de la versión de William. Primero recorre la app con las
cuentas del README; después abre los archivos en este orden. No necesitas memorizar
todo: debes poder explicar qué dato entra, quién lo valida, dónde se guarda y cómo
vuelve a aparecer en pantalla.

## 1. El mapa general

```text
Persona toca o escribe
        ↓
Screen: dibuja y comunica el evento
        ↓
ViewModel: valida y actualiza el estado
        ↓
JuntaRepository: comprueba las reglas que afectan a los datos
        ↓
DAO: ejecuta la consulta en Room
        ↓
Flow avisa el cambio → la pantalla vuelve a dibujarse
```

`MainActivity` inicia Compose y aplica el tema. `AppNavigation` contiene el
`Scaffold`, que organiza barras y márgenes, y el `NavHost`, que muestra la ruta
actual. `Rutas` guarda nombres y `Destinos` define las pestañas por rol.

Una función con `@Composable` describe interfaz. Compose puede ejecutarla otra vez
cuando cambia el estado: por eso no debemos insertar un pago directamente en el
cuerpo de una función de pantalla.

## 2. Entender el estado con el correo

Abrir `LoginUiState`, `LoginViewModel` y `LoginScreen`.

1. El campo muestra `estado.correo`.
2. Al escribir, `onValueChange` llama a `onCorreoChange(valor)`.
3. El ViewModel ejecuta `_estado.update { it.copy(...) }`.
4. `collectAsState()` recibe el cambio y Compose muestra el nuevo texto.

`it` es el estado anterior. `copy(correo = valor)` crea una copia cambiando solo el
correo. El segundo `copy` cambia únicamente `errores.correo` a `null`, sin borrar
el error de la clave. `String?` admite texto o ausencia de texto; `null` significa
que ese campo no tiene un error que mostrar.

`_estado` es mutable y privado: solo el ViewModel lo modifica. `estado` se expone
como `StateFlow`, para observarlo. `val` impide reemplazar la referencia; no convierte
por sí solo todo objeto en inmutable.

La pantalla usa `isError` y `supportingText` para mostrar el error al lado del campo.
El botón llama a la validación; si pasa, el repositorio busca al usuario. Las claves
ficticias se comparan como hash SHA-256.

**Comprueba:** enviar el Login vacío y luego con una clave incorrecta.
**Explícalo:** ¿por qué no sirve dejar `value = ""` y `onValueChange = {}`?

## 3. Sesión, navegación y ViewModel

`LoginViewModel` tiene una sola instancia en `AppNavigation`. Mantiene la sesión al
cambiar de ruta y al recrear la actividad por un giro. Al cerrar sesión se limpia
el historial de navegación, así Atrás no regresa a una pantalla privada.

La sesión no se guarda en Room ni DataStore: si Android termina el proceso, al
volver se solicita Login. Las reservas sí permanecen en Room. Son dos cosas distintas.

Los ViewModels que necesitan abrir Room usan `AndroidViewModel`, que recibe
`Application`. Compose los obtiene mediante `viewModel()`, sin construirlos cada
vez que se dibuja una pantalla. `viewModelScope.launch` ejecuta una tarea suspendida
ligada a la vida del ViewModel.

**Comprueba:** entrar, abrir Nueva reserva, elegir una sala y girar el aparato.
Luego cerrar el proceso y abrirlo de nuevo: pide Login, pero conserva lo guardado.

## 4. Room: Entity, DAO y repositorio

| Pieza | Función | Ejemplo |
|---|---|---|
| Entity | Define columnas de una tabla | `ReservaEntity` |
| DAO | Define consultas SQL y escrituras | `ReservaDao.obtenerPorId` |
| AppDatabase | Reúne tablas y DAO | `AppDatabase.obtener(context)` |
| Repositorio | Coordina consultas y reglas | `JuntaRepository.crearReserva` |
| KSP | Genera implementación al compilar | Configuración de Room en Gradle |

Hay cinco tablas: UsuarioEntity, DependenciaComunitariaEntity, ReservaEntity,
PagoEntity e IngresoEntity. La carga inicial inserta datos ficticios una vez en una
transacción. Las fechas de ejemplo se calculan al cargar, no al abrir cada pantalla.

Una consulta `Flow` vuelve a emitir cuando cambia su tabla. Por eso al registrar un
pago cambian Pagos, Por cobrar, Resumen y la disponibilidad del ticket.

La versión 3 agrega IngresoEntity. `MIGRACION_2_3` ejecuta un `CREATE TABLE` para
conservar lo que ya existía. No usa una actualización que borre la base.

**Comprueba:** guardar una reserva, cerrar y volver a abrir la app.
**Explícalo:** ¿qué diferencia hay entre guardar un campo en el UiState y guardar una fila en Room?

## 5. Reglas de reservas

Abrir `ReglasReserva`, `NuevaReservaViewModel` y `JuntaRepository`.

El formulario exige sala, fecha no pasada, horas válidas entre 09:00 y 22:00,
inicio anterior al fin y aceptación del reglamento. Las fechas son `yyyy-MM-dd` y
las horas `HH:mm`, con ceros a la izquierda; por eso esas cadenas ordenadas se
pueden comparar para el mismo formato.

El cruce usa límites estrictos:

```kotlin
inicioA < finB && inicioB < finA
```

Una reserva de 10:00 a 12:00 se cruza con 11:00 a 13:00, pero no con 12:00 a 14:00.
Solo CONFIRMADA ocupa el horario. Las PENDIENTES pueden coincidir, así que Secretaría
vuelve a revisar el cruce al confirmar.

Una **transacción** reúne la lectura y la escritura como una operación. Sirve para
que dos confirmaciones simultáneas no acepten el mismo horario.

DatePicker entrega una fecha basada en UTC. `fechaDesdeMillis` usa esa misma zona
para no convertir la medianoche en el día anterior al pasar por la zona chilena.

**Comprueba:** crear dos pendientes que coincidan y confirmar ambas: la segunda se rechaza.

## 6. Permisos de los usuarios

| Usuario | Acciones de escritura |
|---|---|
| Vecino | Crear reserva y cancelar una pendiente propia |
| Secretaría | Confirmar o rechazar pendientes |
| Tesorería | Registrar pagos |
| Presidencia | Consulta |

Ocultar el botón ayuda a la persona; no basta para proteger la escritura. El
ViewModel y el repositorio vuelven a comprobar lo que corresponde. El repositorio
consulta al usuario almacenado antes de confirmar una reserva o guardar un pago.

Los vecinos consultan Finanzas sin nombres ni comprobantes. Cada ruta comprueba
el rol antes de mostrar su contenido.

## 7. Registrar pago: el segundo formulario

Abrir `RegistrarPagoUiState`, `ReglasPago`, `RegistrarPagoViewModel` y
`RegistrarPagoScreen`.

- RadioButton: Arriendo o Consumo básico; seleccionar el tipo cambia los campos.
- Arriendo: reserva CONFIRMADA sin pago anterior.
- Consumo: ELECTRICIDAD, AGUA o GAS; no se asocia a una reserva.
- Monto: entero positivo en pesos, hasta nueve dígitos.
- Fecha: real y no futura.
- Descripción: opcional, hasta 240 caracteres.
- Switch: activa el comprobante opcional; si se activa, hay que elegir una imagen.
- Checkbox: acepta que es un registro informativo.

`validarDatosPago` produce mensajes por campo. Al guardar se deshabilita el
formulario para evitar dobles toques. El repositorio relee el estado de la reserva
y comprueba el duplicado dentro de una transacción.

No se llama a un banco. Un arriendo suma ingresos; un consumo suma gastos.
`ResumenViewModel` agrupa por mes y categoría: **saldo = ingresos − gastos**.

**Comprueba:** enviar vacío; luego registrar un arriendo; intentar pagar la misma reserva otra vez.

## 8. Cámara y galería

La pantalla usa `rememberLauncherForActivityResult`: abre otra actividad de Android
y recibe un resultado cuando regresa.

- `TakePicture`: Android toma una foto y la escribe en la URI creada por FileProvider.
- `GetContent`: la persona elige una imagen de la galería/documentos.
- `RequestPermission`: pide permiso de cámara cuando hace falta. Si se deniega,
  se informa y se puede usar galería.
- `AsyncImage` de Coil muestra la vista previa y el comprobante guardado.

Una URI identifica un recurso; no siempre es una ruta de archivo que se pueda abrir
con `File`. Por eso se lee con `contentResolver`.

`guardarComprobante` copia los bytes a la carpeta privada `files/comprobantes`,
comprueba el límite de 20 MB y que sea una imagen. En Room se guarda la URI de esa
copia, no toda la imagen. Si el pago falla, se retira la copia nueva para no dejar
un archivo sin registro. La imagen original de la galería no se elimina.

**Comprueba:** elegir una imagen, guardar el pago y consultar Ver comprobante tras reiniciar.

## 9. Ticket y lectura QR

Abrir `TicketQrScreen`, `QrUtils`, `QrImagen` y `EscanearViewModel`.

Solo se muestra el ticket de una reserva propia CONFIRMADA y pagada. El contenido
incluye versión `JV1`, id de reserva, id de sala, fecha, inicio, fin, nombre y firma.
ZXing convierte ese texto en un bitmap QR; no valida permisos ni horarios.

La firma se calcula con SHA-256 usando los datos y una clave compartida de la demo.
Cambiar la fecha o el nombre sin recalcularla invalida el ticket. Esa clave está en
la app: el diseño es académico, no un control de acceso de producción.

El lector comprueba formato, firma, sala, fecha y ventana. Para una reserva que
empieza a las 18:00 acepta **17:50 a 18:15**, incluidos ambos extremos. No usa la
hora de término como límite de entrada.

El QR completo sirve entre aparatos porque lleva los datos. El código corto solo
lleva id y firma reducida: necesita buscar la reserva y su pago en la base local.
La pestaña Escanear consulta el ticket y no consume su uso.

**Comprueba:** un código inventado, la sala equivocada, un ticket fuera de horario y uno dentro de la ventana.

## 10. Modo puerta

Abrir `PuertaPreferencias`, `PuertaViewModel`, `PuertaScreen` y `LectorQrFrontal`.

Siete toques en Inicia sesión abren la configuración. DataStore guarda el modo,
la sala y el hash de la clave. Al abrir la app, AppNavigation lee esa configuración:
si está activa, muestra la puerta antes de la navegación normal.

CameraX mantiene la cámara frontal y entrega imágenes. ML Kit analiza cada imagen
y devuelve el texto del QR. Se cierra cada imagen analizada para que la cámara
pueda seguir entregando cuadros. Al salir de la pantalla se libera la cámara.

`evaluarPuerta` comprueba firma, sala, horario y usos anteriores. Al aceptar,
`registrarIngreso` inserta el id de reserva en IngresoEntity en una transacción.
Ese id es clave primaria, así no se puede registrar dos veces. El resultado aparece
cuatro segundos; mantener el mismo QR delante no produce una lectura continua.

Atrás abre el diálogo de clave; no desactiva la puerta directamente. La pantalla
mantiene el aparato encendido mientras se usa. Android sigue teniendo sus controles
de sistema: esto no configura una tablet como dispositivo administrado.

El bloqueo de repetición es **por aparato**. No hay servidor compartido ni sincronización.
Preparar todos los tickets de la demostración en un solo aparato emisor evita ids
repetidos entre bases independientes.

## 11. Diseño, animaciones y tamaños

`Color.kt` contiene la paleta y `Theme.kt` asigna los papeles `primary`, `background`,
`surface` y sus colores `on...`. `onPrimary` es el color de contenido que se dibuja
sobre primary. `dynamicColor = false` conserva la paleta elegida.

`ContenidoFormulario` centra el contenido, limita su ancho a 720 dp, permite
scroll y deja espacio al teclado. Resumen organiza sus tres totales en una fila
cuando dispone de 600 dp y en columna en pantallas estrechas. Las listas usan
LazyColumn, que compone los elementos necesarios para la vista.

`AnimatedVisibility` anima errores, el bloque de comprobante y resultados del lector.
Una animación visual acompaña un cambio real de estado; no reemplaza la validación.

## 12. Cómo leer las pruebas

- `ReglasReservaTest` y `PagosQrTest`: reglas puras; corren sin emulador.
- `ReservasRoomTest` y `PagosRecursosTest`: Room, transacciones, migración, imágenes y lectura de QR en Android.
- `LoginNavegacionTest` y `PagosPuertaNavegacionTest`: escriben y tocan la interfaz real.
- Los archivos Example... son pruebas de la plantilla; no prueban reglas del negocio.

Cada prueba prepara un caso, realiza una acción y comprueba el resultado esperado.
Las bases de repositorio son independientes. Las pruebas de pantalla crean y retiran
sus propias filas; ejecutarlas en un emulador destinado a pruebas.

## 13. Preparar la defensa sin memorizar respuestas

1. Dibuja el recorrido de `onCorreoChange` y explica ambos `copy`.
2. Sigue Enviar solicitud hasta el `INSERT` del DAO.
3. Explica por qué se comprueba un cruce dos veces: al solicitar y al confirmar.
4. Muestra por qué Carmen no puede registrar pagos y Luis sí.
5. Diferencia la imagen temporal de cámara de la copia privada del comprobante.
6. Explica qué parte genera el QR y qué parte decide si permite ingresar.
7. Calcula a mano la ventana de una reserva de 19:00 a 21:00.
8. Explica qué conserva Room, qué conserva DataStore y qué conserva el ViewModel.
9. Distingue una prueba pasada de una funcionalidad pendiente de comprobar en un teléfono físico.
10. Haz un cambio pequeño: agrega una validación de descripción y comprueba su mensaje.

Orden sugerido: Login y estado; reservas y Room; permisos y pagos; recursos nativos;
QR y puerta. Al terminar cada bloque, intenta explicarlo con tus palabras sin mirar
el archivo. Las evidencias de colaboración y la defensa no se reemplazan por compilar.
