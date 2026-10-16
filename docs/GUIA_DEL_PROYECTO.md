# Guía del proyecto: etapa de reservas

Esta guía explica el avance de William. No significa que la aplicación completa
esté terminada ni equivale a un porcentaje de la rúbrica.

## 1. Cómo empieza la app

`MainActivity.kt` inicia Compose y aplica `AppJuntaVecinosTheme`.
Dentro del tema se llama a `AppNavigation()`, que contiene el `Scaffold`
(las barras y los márgenes) y el `NavHost` (las pantallas y sus rutas).

`Rutas.kt` guarda los nombres de las rutas. `Destinos.kt` define los elementos
de la barra inferior según el rol. Las pantallas internas tienen botón Volver.

## 2. El circuito del formulario

Ejemplo al escribir el correo:

1. `OutlinedTextField` muestra `estado.correo`.
2. El cambio de texto llama a `onCorreoChange(valor)`.
3. El ViewModel actualiza `_estado` con `update { it.copy(...) }`.
4. La pantalla observa `estado` con `collectAsState()` y se vuelve a dibujar.

`it` representa el estado anterior. `copy(correo = valor)` crea un estado nuevo
y conserva los campos que no se mencionan. El segundo `copy`, sobre `errores`,
limpia solo el error del campo que se está corrigiendo.

La pantalla recibe datos y envía eventos. No decide si la clave es válida ni si
una reserva puede confirmarse.

## 3. Para qué sirve cada clase de Login

- `LoginUiState`: correo, clave, errores e indicador de carga.
- `LoginErrores`: un mensaje nullable por campo y uno general. `null` significa
  que no hay error. Está junto al UiState en `data/model`, siguiendo el contrato actualizado.
- `LoginViewModel`: valida, consulta al repositorio y guarda la sesión.
- `LoginScreen`: dibuja el formulario y sus mensajes.
- `LoginScreenPreview`: dibuja datos de ejemplo, sin iniciar Room.

El ViewModel ahora hereda de `AndroidViewModel` para recibir `Application`,
el contexto que necesita el repositorio al abrir Room. Se obtiene con
`viewModel()`; no se construye manualmente dentro de la pantalla.

La clave se compara mediante `hashClave`. El mismo mensaje general se usa
cuando el correo no existe o la clave es incorrecta.

## 4. Qué hace Room

- Una `Entity` define las columnas de una tabla.
- Un `Dao` contiene consultas y operaciones de esa tabla.
- `AppDatabase` reúne las cuatro tablas y permite acceder a sus DAO.
- `JuntaRepository` ofrece operaciones que usan esos DAO.
- KSP genera el código de Room al compilar.

Una consulta que devuelve `Flow` avisa cuando cambian los datos. Así Mis reservas
se actualiza después de guardar, confirmar o cancelar.

Una transacción reúne varias consultas y una escritura como una sola operación.
En confirmar reserva sirve para comprobar que el horario sigue libre y escribir
la confirmación sin que otra confirmación se interponga.

## 5. Dónde están las reglas de reservas

`NuevaReservaViewModel.validarFormulario()` usa `validarDatosReserva`, una función
de la capa `viewmodel`, para comprobar:

- Dependencia seleccionada.
- Fecha válida y no pasada.
- Horas válidas, de 09:00 a 22:00.
- Inicio anterior al fin.
- Aceptación del reglamento.

El repositorio comprueba los datos que afectan a la escritura y consulta
reservas confirmadas antes de guardar. `horariosSeCruzan` usa:

```kotlin
inicioA < finB && inicioB < finA
```

El límite es estricto: terminar y empezar a la misma hora no es un choque.
Disponibilidad usa esa misma comparación, y muestra las pendientes como
Solicitado sin tratarlas como ocupadas.

La fecha del calendario se convierte con UTC. Usar directamente la zona chilena
sobre la medianoche UTC podría mostrar el día anterior.

## 6. Los permisos

`LoginViewModel` expone los permisos de la sesión. La pantalla los usa para
mostrar los botones adecuados. El ViewModel de gestión también comprueba el
cargo, y el repositorio vuelve a verificarlo antes de escribir.

- Vecino: solicita y cancela únicamente sus propias pendientes.
- Secretaría: confirma o rechaza pendientes.
- Tesorería y Presidencia: consulta de reservas en esta etapa.
- Finanzas: consulta sin nombres para vecinos.

Ocultar un botón no reemplaza la validación de la operación.

## 7. Qué falta y en qué orden

1. Formulario Registrar pago: tipo, reserva/categoría, monto, fecha y validaciones.
2. Cámara y galería para adjuntar un comprobante y conservarlo en archivos privados.
3. Listado de pagos, autorización de Tesorería y prevención de pagos duplicados.
4. Generación de ticket QR para una reserva confirmada y pagada.
5. Lectura del QR y validación de dependencia y ventana de ingreso.
6. Pruebas finales en teléfono, adaptación de pantallas, accesibilidad y revisión de rúbrica.
7. Preparación de entrega y evidencias reales de colaboración.

Las pestañas pendientes están identificadas. El resumen actual usa los pagos
ficticios persistidos; no crea pagos nuevos.

## 8. Ejercicios para preparar la defensa

- Explica por qué `_estado` es privado y `estado` se expone como StateFlow.
- Cambia un texto del Login y ubica el color del botón en el tema.
- Añade una validación al formulario de reserva y su mensaje visual.
- Sigue una solicitud desde el botón Enviar hasta el DAO.
- Explica por qué dos pendientes pueden coincidir, pero dos confirmadas no.
- Muestra que la reserva sigue guardada después de reiniciar el proceso.

Primero lee cada pieza con calma y luego intenta explicarla sin mirar esta guía.
